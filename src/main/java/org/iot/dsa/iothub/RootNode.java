package org.iot.dsa.iothub;

import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * This is the root node of the link.
 *
 * @author Daniel Shapiro
 */
public class RootNode extends DSRootNode {
    
    private static DSIRequester requester;

    private void handleAddIotHub(DSMap parameters) {
        String name = parameters.getString("Name");
        String connString = parameters.getString("Connection String");

        IotHubNode hub = new IotHubNode(connString);
        add(name, hub);
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((RootNode) info.getParent()).handleAddIotHub(invocation.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Connection String", DSValueType.STRING, null);
        declareDefault("Add IoT Hub", act);
    }
    
    @Override
    protected void onStarted() {
        setRequester(getLink().getConnection().getRequester());
    }

    public static DSIRequester getRequester() {
        return requester;
    }

    public static void setRequester(DSIRequester requester) {
        RootNode.requester = requester;
    }
}
