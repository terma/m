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
import com.github.terma.m.shared.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class MinTimestampCallback implements ArrayLayoutCallback {

    private final long[] timestamps;
    private final Map<Short, Acc[]> result = new HashMap<>();
    private long min = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

    public MinTimestampCallback(final FastSelect<Event> fastSelect) {
        this.timestamps = ((LongData) fastSelect.getColumnsByNames().get("timestamp").data).data;
    }

    @Override
    public void data(int position) {
        min = Math.min(min, timestamps[position]);
    }

    public long getResult() {
        return min;
    }

}
