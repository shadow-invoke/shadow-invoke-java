package org.shadow.invoke.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@ToString
@AllArgsConstructor
public class FieldFilter {
    private final Class<?> filteredClass;
    private final Set<String> filteredFields;

    public static FieldFilter from(Class<?> filteredClass) {
        return new FieldFilter(filteredClass, new HashSet<>());
    }

    public FieldFilter fields(String... filteredFieldNames) {
        if(filteredFieldNames != null && filteredFieldNames.length > 0) {
            this.filteredFields.addAll(Arrays.asList(filteredFieldNames));
        }
        return this;
    }

    public boolean isValid() {
        return this.filteredClass != null && this.filteredFields != null && !this.filteredFields.isEmpty();
    }
}
