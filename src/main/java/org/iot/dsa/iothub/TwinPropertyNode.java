package org.iot.dsa.iothub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.iothub.node.BoolNode;
import org.iot.dsa.iothub.node.DoubleNode;
import org.iot.dsa.iothub.node.ListNode;
import org.iot.dsa.iothub.node.StringNode;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * A node that represents a Device Twin Property or Tag whose value is a map.
 *
 * @author Daniel Shapiro
 */
public class TwinPropertyNode extends DSNode implements TwinProperty, TwinPropertyContainer {

    private Set<String> nulls = new HashSet<String>();

    public TwinPropertyNode() {
    }

    @Override
    public Object getObject() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String name : nulls) {
            map.put(name, null);
        }
        for (DSInfo info : this) {
            if (!info.isAction()) {
                String name = info.getName();
                DSIObject value = info.get();
                if (value instanceof TwinProperty) {
                    map.put(name, ((TwinProperty) value).getObject());
                }
            }
        }
        return map;
    }

    public void handleAdd(String name, String type) {
        TwinProperty vn;
        switch (type.charAt(0)) {
            case 'S':
                vn = new StringNode();
                break;
            case 'N':
                vn = new DoubleNode();
                break;
            case 'B':
                vn = new BoolNode();
                break;
            case 'L':
                vn = new ListNode();
                break;
            case 'M':
                vn = new TwinPropertyNode();
                break;
            default:
                vn = null;
                break;
        }
        if (vn != null) {
            put(name, vn);
            if (vn instanceof DSNode) {
                onChange(((DSNode) vn).getInfo());
            }
        }
    }

    @Override
    public void onChange(DSInfo info) {
        if (info.isAction()) {
            return;
        }
        ((TwinPropertyContainer) getParent()).onChange(getInfo());
    }

    @Override
    public void onDelete(DSInfo info) {
        nulls.add(info.getName());
        onChange(info);
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Add", makeAddAction());
    }

    @Override
    protected void onChildChanged(DSInfo info) {
        onChange(info);
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();
        DSNode parent = getParent();
        if (parent instanceof TwinPropertyContainer) {
            ((TwinPropertyContainer) parent).onDelete(getInfo());
        }
    }

    private void invokeAdd(DSMap parameters) {
        String name = parameters.getString("Name");
        String vt = parameters.getString("Value Type");
        handleAdd(name, vt);
    }

    private static DSAction makeAddAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                ((TwinPropertyNode) req.getTarget()).invokeAdd(req.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSString.NULL, null);
        act.addParameter("Value Type", DSFlexEnum.valueOf("String", Util.getSimpleValueTypes()),
                         null);
        return act;
    }
}
