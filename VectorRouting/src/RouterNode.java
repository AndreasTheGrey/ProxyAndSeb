public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator5 sim;
    private int[] routeBy = new int[RouterSimulator5.NUM_NODES];
    private int[][] myTable;

    private int[] neighbours = new int[RouterSimulator5.NUM_NODES];
    private int[] nonNeighbours = new int[RouterSimulator5.NUM_NODES];

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator5 sim, int[] costs) {
        myID = ID;
        this.sim = sim;
        myGUI = new GuiTextArea("  Output window for Router #" + ID + "  ");
        System.out.println("Constructor is run");

        myTable = new int[RouterSimulator5.NUM_NODES][RouterSimulator5.NUM_NODES];

        int index = 0;
        int index2 = 0;
        for (int i = 0; i < costs.length; i++) {
            if(myID != i) {
                if (costs[i] != 999) {
                    neighbours[index++] = i;
                    myGUI.println("My neighbour " + i);
                }else{
                    nonNeighbours[index2++] = i;
                    myGUI.println("My non neighbour " + i);
                }
            }
        }


        for (int row = 0; row < costs.length; row++) {
            myTable[myID][row] = costs[row];
        }

    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {
        myGUI.println("Updating a whole table!");
        System.arraycopy(pkt.mincost,0,myTable[pkt.sourceid],0,RouterSimulator5.NUM_NODES);

        printDistanceTable();

        boolean updated = recalculateOwn();
        if(updated) {
            sendUpdate();
        }

    }



    private boolean recalculateOwn(){
        boolean recalculated = false;

        int[] shortestPath = new int[RouterSimulator5.NUM_NODES];


        for (int i = 0; i < RouterSimulator5.NUM_NODES; i++) {
            shortestPath[i] = 999;
        }


        for (int host = 0; host < myTable.length; host++) {
            if (host != myID) {
                //update our own route by table and choose the least costy route
                for (int values = 0; values < myTable.length; values++) {
                   // Ska vara host på andra termen ist för values... buggar doock så ändrade tillbaka.
                    int newValue = myTable[host][values] + myTable[myID][values];
                    if (shortestPath[values] > newValue) {
                        shortestPath[values] = newValue;
                        routeBy[values] = host;
                        recalculated = true;
                        myGUI.println("Changing routeBy value to " + host);
                    }
                }
            }
        }
        if(recalculated) {
            System.arraycopy(shortestPath, 0, myTable[myID], 0, RouterSimulator5.NUM_NODES);
        }
        return recalculated;

    }



    //--------------------------------------------------//
    private void sendUpdate() {
        for (int id = 0; id < RouterSimulator5.NUM_NODES; id++) {

            if (id != myID) {

                int[] tempValues = new int[RouterSimulator5.NUM_NODES];
                System.arraycopy(myTable[myID],0,tempValues,0,RouterSimulator5.NUM_NODES);

                if(routeBy[id] == id){
                    tempValues[id] = 999;
                }

                RouterPacket packet = new RouterPacket(myID, id, tempValues);
                sim.toLayer2(packet);
            }
        }
    }


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
        myGUI.println("Updating Link Cost");
        myTable[myID][dest] = newcost;
        sendUpdate();
    }

}
