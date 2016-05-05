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

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
class GigaSpaceJmxUrlLocator extends JmxUrlLocator {

    private static final Logger LOGGER = Logger.getLogger(GigaSpaceJmxUrlLocator.class.getName());

    private static final String GIGA_SPACE_LOCATORS_PARAM = "gigaSpaceLocators";
    private static final String GIGA_SPACE_USER_PARAM = "gigaSpaceUser";
    private static final String GIGA_SPACE_PASSWORD_PARAM = "gigaSpacePassword";

    private final Map<String, String> params;
    private final Admin admin;

    public GigaSpaceJmxUrlLocator(Map<String, String> params) {
        this.params = params;

        LOGGER.info("Connection to GS Admin...");

        String locators = params.get(GIGA_SPACE_LOCATORS_PARAM);
        LOGGER.info("GS locators: " + locators);

        final AdminFactory adminFactory = new AdminFactory();
        adminFactory.useDaemonThreads(true);
        adminFactory.useGsLogging(false);
        if (params.containsKey(GIGA_SPACE_USER_PARAM)) {
            adminFactory.userDetails(params.get(GIGA_SPACE_USER_PARAM), params.get(GIGA_SPACE_PASSWORD_PARAM));
        }
        adminFactory.addLocators(locators);

        admin = adminFactory.createAdmin();

        LOGGER.info("Waiting when admin get info...");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // admin.close();
    }

    @Override
    public List<ValueWithContext<String>> get() {
        List<ValueWithContext<String>> jmxUrls = new ArrayList<>();

        for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
            final String jmxUrl = gridServiceContainer.getVirtualMachine().getDetails().getJmxUrl();
            jmxUrls.add(
                    new ValueWithContext<>(jmxUrl, params)
                            .withProperty("containerId", String.valueOf(gridServiceContainer.getAgentId()))
                            .withProperty("containerType", "GSC")
            );
        }

        return jmxUrls;
    }

}
