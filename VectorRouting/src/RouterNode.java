import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] routeBy = new int[RouterSimulator.NUM_NODES];
    private int[][] myTable;

    //private int[] neighbours = new int[RouterSimulator.NUM_NODES];
    //private int[] nonNeighbours = new int[RouterSimulator.NUM_NODES];
    private Set<Integer> neighbours = new HashSet<Integer>();
    private Set<Integer> nonNeighbours = new HashSet<Integer>();
    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
        myID = ID;
        this.sim = sim;
        myGUI = new GuiTextArea("  Output window for Router #" + ID + "  ");

        myTable = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

        for (int i = 0; i < costs.length; i++) {
            if(myID != i) {
                if (costs[i] != 999) {
                    neighbours.add(i);
                    myGUI.println("My neighbour " + i);
                }else{
                    nonNeighbours.add(i);
                    myGUI.println("My non neighbour " + i);
                }
            }
        }


        for (int row = 0; row < costs.length; row++) {
            myTable[myID][row] = costs[row];
            if(row != myID) {
                routeBy[row] = row;
            }
        }
        printDistanceTable();
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {

        myGUI.print("Received packet from node " + pkt.sourceid + ":   [ ");
        for (int i = 0; i < pkt.mincost.length - 1; i++) {
            myGUI.print(pkt.mincost[i] + " ,");
        }
        myGUI.println(pkt.mincost[pkt.mincost.length - 1] + "]");
        System.arraycopy(pkt.mincost,0,myTable[pkt.sourceid],0, RouterSimulator.NUM_NODES);

        boolean updated = recalculateOwn();
        printDistanceTable();
        if(updated) {
            sendUpdate();
        }

    }



    private boolean recalculateOwn(){
        boolean recalculated = false;

        int[] shortestPath = new int[RouterSimulator.NUM_NODES];
        System.arraycopy(myTable[myID],0,shortestPath,0, RouterSimulator.NUM_NODES);
        for (int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            //shortestPath[i] = 999;
        }


        for (int node = 0; node < RouterSimulator.NUM_NODES; node++) {
            //dont update our own table
            if(node != myID){

                for (int row = 0; row < RouterSimulator.NUM_NODES; row++) {
                    if(myTable[node][row] != 0 && myTable[node][row] != RouterSimulator.INFINITY){
                        if(isBetterPath(node,row,shortestPath)){
                            insertNewPath(node,row,shortestPath);
                            routeBy[node] = row;
                            recalculated = true;
                            myGUI.println("Found shorter path, updating node " + node + " via " + row + " with value " +
                                    myTable[node][row] + myTable[myID][node]);
                        }
                    }
                }
            }
        }

        if(recalculated) {
            System.arraycopy(shortestPath, 0, myTable[myID], 0, RouterSimulator.NUM_NODES);
        }
        return recalculated;

    }

    private void insertNewPath(int node, int row, int[] shortestPath) {
        shortestPath[row] = myTable[node][row] + myTable[myID][node];
    }


    private boolean isBetterPath(int node, int row, int[] shortestPath) {
        //check if the new routing is better than what I already have
        return myTable[node][row] + myTable[myID][node] < shortestPath[row];
    }


    //--------------------------------------------------//
    private void sendUpdate() {
        //TODO we should not send updates to NON neighbours
        myGUI.print("Sending update from node " + myID);
        for (int id = 0; id < RouterSimulator.NUM_NODES; id++) {

            if (id != myID && !nonNeighbours.contains(id)) {

                int[] tempValues = new int[RouterSimulator.NUM_NODES];
                System.arraycopy(myTable[myID],0,tempValues,0, RouterSimulator.NUM_NODES);

                for (int row = 0; row < RouterSimulator.NUM_NODES; row++) {
                    //loop through and set routes based on a path(id) to infinity
                    if(routeBy[row] == id) {
                        tempValues[row] = RouterSimulator.INFINITY;
                    }
                }

                tempValues[myID] = 0;
                tempValues[id] = RouterSimulator.INFINITY;
                myGUI.print(" "+ Arrays.toString(tempValues) + " ");
                RouterPacket packet = new RouterPacket(myID, id, tempValues);
                sim.toLayer2(packet);
            }
        }
        myGUI.println();
    }

    /*for (int host = 0; host < myTable.length; host++) {
            //we should only update if our own via table is updated.
            if (host != myID) {
                //update our own route by table and choose the least costy route
                for (int values = 0; values < myTable.length; values++) {
                   // Ska vara host på andra termen ist för values... buggar doock så ändrade tillbaka.
                    int newValue = myTable[host][values] + myTable[myID][host];

                    if (shortestPath[values] > newValue) {
                        shortestPath[values] = newValue;
                        routeBy[values] = host;
                        recalculated = true;
                        myGUI.println("Changing routeBy value to " + host);
                    }
                }
            }
        }*/


    //--------------------------------------------------
    public void printDistanceTable() {
        myGUI.println("Current table for " + myID +
                "  at time " + sim.getClocktime());
        for (int row = 0; row < myTable.length; row++) {
            for (int col = 0; col < myTable.length; col++) {
                myGUI.print(String.valueOf(myTable[col][row]) + "   ");
            }
            myGUI.println("");
        }

        myGUI.println("Routes via");
        for (int col = 0; col < myTable.length; col++) {
            myGUI.print(routeBy[col] + " ");
        }
        myGUI.println("");

    }

    //--------------------------------------------------
    public void updateLinkCost(int dest, int newcost) {
        myGUI.println("Updating link cost between " + myID + " - " + dest + " to " + newcost);
        myTable[myID][dest] = newcost;
        routeBy[dest] = dest;
        sendUpdate();
    }

}
