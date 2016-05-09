/*

    Copyright 2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.terma.m.node.jmx;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ValueWithContext<T> {

    private final T value;
    private final Map<String, String> context;

    public ValueWithContext(T value, Map<String, String> context) {
        this.value = value;
        this.context = new HashMap<>(context);
    }

    public ValueWithContext(T value) {
        this(value, new HashMap<String, String>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueWithContext<?> that = (ValueWithContext<?>) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return context != null ? context.equals(that.context) : that.context == null;

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        return "ValueWithContext {" + "value: " + value + ", context: " + context + '}';
    }

    public ValueWithContext<T> withProperty(String key, String value) {
        context.put(key, value);
        return this;
    }

    public T getValue() {
        return value;
    }

    public Map<String, String> getContext() {
        return context;
    }

}
