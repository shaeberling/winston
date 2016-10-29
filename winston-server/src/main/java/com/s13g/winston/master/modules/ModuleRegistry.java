/*
 * Copyright 2016 The Winston Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.s13g.winston.master.modules;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.modules.instance.WemoModule;
import com.s13g.winston.proto.Master;
import com.sun.org.apache.xpath.internal.operations.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Central class to access all module creators.
 */
public class ModuleRegistry {
  private static final Logger LOG = LogManager.getLogger(ModuleRegistry.class);
  private static final List<Class<? extends ModuleCreator>> sCreatorClasses = initCreatorsList();

  private final ModuleContext mModuleContext;
  private final Map<String, ModuleCreator> mCreators;
  private Collection<Module> mActiveModules;

  public ModuleRegistry(ModuleContext moduleContext, Master.MasterConfig config) {
    mModuleContext = moduleContext;
    mCreators = createCreators()
        .stream()
        .collect(Collectors.toMap(
            ModuleCreator::getType,
            Function.identity()));
    initialize(config);
  }

  /**
   * @return A list of all active modules.
   */
  public Collection<Module> getActiveModules() {
    return mActiveModules;
  }

  /**
   * Creates all the modules from the given configuration.
   *
   * @param config the configuration
   */
  private void initialize(Master.MasterConfig config) {
    List<Module> modules = new LinkedList<>();
    for (Master.Module module : config.getModuleList()) {
      String type = module.getType();
      if (!mCreators.containsKey(type)) {
        LOG.error("Cannot find module creator for type: " + type);
        continue;
      }
      try {
        modules.add(mCreators.get(type).create(
            mModuleContext,
            new ModuleParameters(module.getParamList())));
      } catch (ModuleCreationException e) {
        LOG.error("Unable to create module of type: " + type, e);
      }
    }
    mActiveModules = ImmutableList.copyOf(modules);
    LOG.info("Active modules: " + mActiveModules.size());
  }

  private List<ModuleCreator> createCreators() {
    List<ModuleCreator> creators = new ArrayList<>();
    for (Class<? extends ModuleCreator> creatorClass : sCreatorClasses) {
      try {
        creators.add(creatorClass.newInstance());
      } catch (InstantiationException | IllegalAccessException ex) {
        LOG.error("Cannot instantiate creator.", ex);
      }
    }
    return ImmutableList.copyOf(creators);
  }

  private static List<Class<? extends ModuleCreator>> initCreatorsList() {
    List<Class<? extends ModuleCreator>> list = new ArrayList<>();
    list.add(WemoModule.Creator.class);
    // Add more here ...
    // TODO: Maybe make this dynamic, or add the supported classes to a config file.
    return list;
  }

}
