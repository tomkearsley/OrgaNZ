package com.humanharvest.organz.utilities.type_converters;

import com.humanharvest.organz.utilities.enums.BloodType;

/**
 * Converter used by PicoCLI options to select bloodtypes from strings
 */
public class BloodTypeConverter implements TypeConverter<BloodType> {

    /**
     * Convert a string to a BloodType, matches case insensitive
     * @param value String input from user via PicoCLI
     * @return BloodType object
     * @throws TypeConversionException Throws exception if invalid blood type
     */
    @Override
    public BloodType convert(Object value) throws TypeConversionException {
        try {
            return BloodType.fromString(value.toString());
        } catch (IllegalArgumentException e) {
            throw new TypeConversionException(e.getMessage());
        }
    }
}
