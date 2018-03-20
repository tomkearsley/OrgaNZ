package seng302.Actions;

import seng302.Donor;
import seng302.Utilities.PrimitiveConverter;

import java.beans.Statement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A reversible donor modification Action
 */
public class ModifyDonorAction implements Action {

    private Map<String, Object> executors = new HashMap<>();
    private Map<String, Object> unExecutors = new HashMap<>();
    private Donor donor;
    private Method[] donorMethods;

    /**
     * Create a new Action
     * @param donor The donor to be modified
     */
    public ModifyDonorAction(Donor donor) {
        this.donor = donor;
        donorMethods = donor.getClass().getMethods();
    }

    /**
     * Add a modification to the donor
     * @param field The setter field of the donor. Must match a valid setter in the Donor object
     * @param oldValue The object the field initially had. Should be taken from the Donors equivalent getter
     * @param newValue The object the field should be update to. Must match the setters Object type
     */
    public void addChange(String field, Object oldValue, Object newValue) throws NoSuchMethodException, NoSuchFieldException {
        for (Method m : donorMethods) {
            if (m.getName().equals(field)) {
                if (m.getParameterCount() != 1) {
                    throw new NoSuchFieldException("Method expects more than one field");
                }
                PrimitiveConverter converter = new PrimitiveConverter();
                Class<?> expectedClass = converter.convertToWrapper(m.getParameterTypes()[0]);
                if ((newValue != null && newValue.getClass() != expectedClass) || (oldValue != null && oldValue.getClass() != expectedClass)) {
                    throw new NoSuchFieldException("Method expects a different field type than the one given");
                }
                executors.put(field, newValue);
                unExecutors.put(field, oldValue);
                return;
            }
        }
        throw new NoSuchMethodException("Donor does not contain that method");
    }

    @Override
    public void execute() {
        runChanges(executors);
    }

    @Override
    public void unExecute() {
        runChanges(unExecutors);
    }

    /**
     * Loops through a map of String, Objects and applies the Java.beans.Statement to it using the String as the setter and the Object as the parameter
     * @param map A map of String, Objects
     */
    private void runChanges(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                Object[] var = {entry.getValue()};
                Statement s = new Statement(donor, entry.getKey(), var);
                s.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
