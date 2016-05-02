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
package com.github.terma.m.server;

import com.github.terma.fastselect.FastSelect;
import com.github.terma.fastselect.callbacks.ArrayLayoutCallback;
import com.github.terma.fastselect.data.LongData;
import com.github.terma.fastselect.data.ShortData;
import com.github.terma.m.shared.Event;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
class DataCallback implements ArrayLayoutCallback {

    private final long[] values;
    private final long[] timestamps;
    private final short[] metricCodes;

    private final int parts;
    private final long min;
    private final int del;

    private final Map<Short, Acc[]> result = new HashMap<>();

    public DataCallback(final FastSelect<Event> fastSelect, final int parts, final long min, final long max) {
        this.values = ((LongData) fastSelect.getColumnsByNames().get("value").data).data;
        this.timestamps = ((LongData) fastSelect.getColumnsByNames().get("timestamp").data).data;
        this.metricCodes = ((ShortData) fastSelect.getColumnsByNames().get("metricCode").data).data;

        this.parts = parts;
        this.min = min;
        this.del = (int) Math.ceil((float) (max - min) / (float) parts);
    }

    @Override
    public void data(int position) {
        if (del == 0) return;

        final short event = metricCodes[position];
        final long timestamp = timestamps[position];
        final int chunk = (int) ((timestamp - min) / del);

        final Acc[] accs = getAccs(event);
        final Acc acc = accs[chunk];
        acc.value += values[position];
        acc.count++;
    }

    private Acc[] getAccs(final short event) {
        Acc[] accs = result.get(event);
        if (accs == null) {
            accs = createAccs();
            result.put(event, accs);
        }
        return accs;
    }

    private Acc[] createAccs() {
        final Acc[] accs = new Acc[parts + 1]; // add one acc w/o data for representation
        long timestamp = min;
        for (int i = 0; i < accs.length; i++) {
            accs[i] = new Acc(timestamp);
            timestamp += del;
        }
        return accs;
    }

    public Map<Short, Acc[]> getResult() {
        return result;
    }

}
