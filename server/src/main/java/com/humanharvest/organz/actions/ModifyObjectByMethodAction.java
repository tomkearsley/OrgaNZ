package com.humanharvest.organz.actions;

import java.beans.Statement;
import java.lang.reflect.Method;
import java.util.Objects;

import com.humanharvest.organz.utilities.type_converters.PrimitiveConverter;

/**
 * Create a new modification on any object using it's field name and the old and new values
 * Exceptions are thrown if the object does not contain that field, or if the values are the wrong type
 * If you have a field such as a password, use the isPrivate boolean to ensure the values are not leaked
 */
public class ModifyObjectByMethodAction extends Action {

    private Object toModify;
    private String field;
    private Object[] oldValue;
    private Object[] newValue;
    private boolean isPrivate;

    /**
     * Create a new modification
     * @param toModify The object to be modified
     * @param field The setter field of the object. Must match a valid setter
     * @param oldValue The object the field initially had. Should be taken from the objects equivalent getter
     * @param newValue The object the field should be update to. Must match the setters object type
     * @throws NoSuchMethodException Thrown if the object does not have the field specified
     * @throws NoSuchFieldException Thrown if the object setter expected type does not match one of the value types
     */
    public ModifyObjectByMethodAction(Object toModify, String field, Object oldValue, Object newValue)
            throws NoSuchMethodException, NoSuchFieldException {
        setupAction(toModify, field, oldValue, newValue);
    }

    /**
     * Create a new modification with the option to hide the values
     * @param toModify The object to be modified
     * @param field The setter field of the object. Must match a valid setter
     * @param oldValue The object the field initially had. Should be taken from the objects equivalent getter
     * @param newValue The object the field should be update to. Must match the setters object type
     * @param isPrivate If set to true, the text returns will only return the field, not the values
     * @throws NoSuchMethodException Thrown if the object does not have the field specified
     * @throws NoSuchFieldException Thrown if the object setter expected type does not match one of the value types
     */
    public ModifyObjectByMethodAction(Object toModify, String field, Object oldValue, Object newValue, boolean isPrivate)
            throws NoSuchMethodException, NoSuchFieldException {
        setupAction(toModify, field, oldValue, newValue);
        this.isPrivate = isPrivate;
    }

    private void setupAction(Object toModify, String field, Object oldValue, Object newValue)
            throws NoSuchMethodException, NoSuchFieldException {
        Method[] objectMethods = toModify.getClass().getMethods();
        for (Method method : objectMethods) {
            if (Objects.equals(method.getName(), field)) {
                if (method.getParameterCount() != 1) {
                    throw new NoSuchFieldException("Method expects more than one field");
                }
                PrimitiveConverter converter = new PrimitiveConverter();
                Class<?> expectedClass = converter.convertToWrapper(method.getParameterTypes()[0]);
                if ((newValue != null && newValue.getClass() != expectedClass)
                        || oldValue != null && oldValue.getClass() != expectedClass) {
                    throw new NoSuchFieldException("Method expects a different field type than the one given");
                }
                this.toModify = toModify;
                this.field = field;
                this.oldValue = new Object[]{oldValue};
                this.newValue = new Object[]{newValue};
                return;
            }
        }
        throw new NoSuchMethodException("Object does not contain that method");
    }

    @Override
    public void execute() {
        runChange(newValue);
    }

    @Override
    public void unExecute() {
        runChange(oldValue);
    }

    private static String unCamelCase(String inCamelCase) {
        String unCamelCased = inCamelCase.replaceAll("([a-z])([A-Z]+)", "$1 $2");
        return unCamelCased.substring(0, 1).toUpperCase() + unCamelCased.substring(1);
    }

    private static String formatValue(Object[] value) {
        return value[0] != null ? String.format("'%s'", value[0].toString()) : "empty";
    }

    @Override
    public String getExecuteText() {
        if (isPrivate) {
            return unCamelCase(field) + ".";
        } else {
            return String.format("%s from %s to %s.", unCamelCase(field), formatValue(oldValue), formatValue(newValue));
        }
    }

    @Override
    public String getUnexecuteText() {
        if (isPrivate) {
            return unCamelCase(field) + ".";
        } else {
            return String.format("%s from %s to %s.", unCamelCase(field), formatValue(newValue), formatValue(oldValue));
        }
    }

    @Override
    public Object getModifiedObject() {
        return toModify;
    }

    /**
     * Execute a statement update on the object. Should not throw the errors from Statement as we check them in the
     * constructor
     * @param value Value to set
     */
    private void runChange(Object[] value) {
        try {
            Statement statement = new Statement(toModify, field, value);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
