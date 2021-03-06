package com.humanharvest.organz.utilities.type_converters;

import com.humanharvest.organz.utilities.enums.Gender;
import picocli.CommandLine;

/**
 * Converter used by PicoCLI options to select genders from strings
 */
public class GenderConverter implements CommandLine.ITypeConverter<Gender> {

    /**
     * Convert a string to a Gender, matches case insensitive
     * @param value String input from user via PicoCLI
     * @return Gender object
     * @throws CommandLine.TypeConversionException Throws exception if gender type
     */
    @Override
    public Gender convert(String value) throws CommandLine.TypeConversionException {
        try {
            return Gender.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new CommandLine.TypeConversionException(e.getMessage());
        }
    }
}
