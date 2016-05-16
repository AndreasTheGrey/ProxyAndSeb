import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RouterNode {
    private int myRouter;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] routeBy = new int[RouterSimulator.NUM_NODES];
    private int[][] myTable;
    private int[] myRawCosts;

    public static boolean POISON_REVERSE = true;

    private Set<Integer> neighbours = new HashSet<Integer>();
    private Set<Integer> nonNeighbours = new HashSet<Integer>();

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
        myRouter = ID;
        this.sim = sim;
        myGUI = new GuiTextArea("  Output window for Router #" + ID + "  ");
        myRawCosts = new int[RouterSimulator.NUM_NODES];
        myTable = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

        for (int i = 0; i < costs.length; i++) {
            if (myRouter != i) {
                if (costs[i] != 999) {
                    neighbours.add(i);
                    myGUI.println("My neighbour " + i);
                } else {
                    nonNeighbours.add(i);

                    for (int j = 0; j < RouterSimulator.NUM_NODES; j++) {
                        myTable[i][j] = RouterSimulator.INFINITY;
                    }
                    myGUI.println("My non neighbour " + i);
                }
            }
        }


        for (int row = 0; row < costs.length; row++) {
            myTable[myRouter][row] = costs[row];
            routeBy[row] = row;
        }
        System.arraycopy(costs, 0, myRawCosts, 0, RouterSimulator.NUM_NODES);
        sendUpdate();
        printDistanceTable();
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {

        myGUI.print("Received packet from node " + pkt.sourceid + ":   [ ");
        for (int i = 0; i < pkt.mincost.length - 1; i++) {
            myGUI.print(pkt.mincost[i] + " ,");
        }
        myGUI.println(pkt.mincost[pkt.mincost.length - 1] + "]");
        System.arraycopy(pkt.mincost, 0, myTable[pkt.sourceid], 0, RouterSimulator.NUM_NODES);

        boolean updated = updateTable();
        printDistanceTable();
        if (updated) {
            sendUpdate();
        }

    }


    /**
     * Updates the table, returns true if any updates occured.
     *
     * @return boolean
     */
    public boolean updateTable() {
        /*
            VARIABLES
            ----------
            myTable
            myRouter
            RouterSimulator.NUM_NODES
            RouterSimulator.INFINITY
            ----------
         */

        // Save our old table.
        int[] oldTable = new int[RouterSimulator.NUM_NODES];
        System.arraycopy(myTable[myRouter], 0, oldTable, 0, RouterSimulator.NUM_NODES);
        // Reset our costs to all routers, since we MIGHT change them.
        System.arraycopy(myRawCosts, 0, myTable[myRouter], 0, RouterSimulator.NUM_NODES);

        // Reset the routes to original state.
        for (int row = 0; row < RouterSimulator.NUM_NODES; row++) {
            routeBy[row] = row;
        }

        // Iterate through our table, to sum the costs and overwrite current if the
        // new cost is less.
        for (int router = 0; router < RouterSimulator.NUM_NODES; router++) {
            if (router != myRouter) {
                for (int index = 0; index < RouterSimulator.NUM_NODES; index++) {
                    if (index != myRouter) {
                        myGUI.println("Checking router: " + router + ".");
                        myGUI.println("Cost to router: " + index + " is " + myTable[router][index] + ".");
                        int costToRoute = myTable[myRouter][router] + myTable[router][index];
                        if (costToRoute < myTable[myRouter][index]) {
                            myGUI.println("COST WAS LESS!");
                            myGUI.println("Old cost: " + myTable[myRouter][index] + ".");
                            myGUI.println("New cost: " + costToRoute + " routed through " + router + ".");
                            myTable[myRouter][index] = costToRoute;
                            routeBy[index] = router;
                        }
                    }
                }
            }
        }

        // Compare the old table with the new to see if we have to send any updates or if the values are the same.
        boolean shouldSendUpdates = false;
        for (int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            if (oldTable[i] != myTable[myRouter][i]) {
                shouldSendUpdates = true;
                break;
            }
        }

        return shouldSendUpdates;

    }

    //--------------------------------------------------//
    private void sendUpdate() {
        myGUI.print("Sending update from node " + myRouter);
        for (int router = 0; router < RouterSimulator.NUM_NODES; router++) {

            if (router != myRouter && !nonNeighbours.contains(router)) {

                int[] tempValues = new int[RouterSimulator.NUM_NODES];
                System.arraycopy(myTable[myRouter], 0, tempValues, 0, RouterSimulator.NUM_NODES);

                if (POISON_REVERSE) {
                    for (int index = 0; index < RouterSimulator.NUM_NODES; index++) {
                        //loop through and set routes based on a path(id) to infinity
                        if (routeBy[index] == router) {
                            tempValues[index] = RouterSimulator.INFINITY;
                        }
                    }

                    tempValues[myRouter] = 0;
                    tempValues[router] = RouterSimulator.INFINITY;
                }
                myGUI.print(" " + Arrays.toString(tempValues) + " ");
                RouterPacket packet = new RouterPacket(myRouter, router, tempValues);
                sim.toLayer2(packet);
            }
        }
        myGUI.println();
    }

    //--------------------------------------------------
    public void printDistanceTable() {
        myGUI.println("Current table for " + myRouter +
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
        if (neighbours.contains(dest)) {
            myGUI.println("Updating link cost between " + myRouter + " - " + dest + " to " + newcost);
            myRawCosts[dest] = newcost;
            boolean updated = updateTable();
            if (updated) {
                sendUpdate();
            }
        }
    }

}
