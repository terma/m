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
package com.github.terma.m.node.gigaspace;

import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

@SuppressWarnings("WeakerAccess")
class GigaSpaceUtils {

    public static void registerType(GigaSpace gigaSpace, final String typeName) {
        SpaceTypeDescriptor typeDescriptor = new SpaceTypeDescriptorBuilder(typeName)
                .idProperty("A").create();
        gigaSpace.getTypeManager().registerTypeDescriptor(typeDescriptor);
    }

    public static GigaSpace getGigaSpace(final String url, final String user, final String password) {
        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer(url);
        if (user != null) urlSpaceConfigurer.userDetails(user, password);
        return new GigaSpaceConfigurer(urlSpaceConfigurer.create()).create();
    }
}
