/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.windup.ui.internal.rules;

import java.io.File;

import org.jboss.tools.windup.model.domain.ModelService;
import org.jboss.tools.windup.ui.internal.rules.xml.XMLRulesetModelUtil;
import org.jboss.tools.windup.windup.CustomRuleProvider;
import org.jboss.windup.tooling.rules.RuleProvider;
import org.jboss.windup.tooling.rules.RuleProvider.RuleProviderType;
import org.jboss.windup.tooling.rules.RuleProviderRegistry;

public class RulesNode {
	
	protected RuleProviderRegistry ruleProviderRegistry;
	
	public RulesNode (RuleProviderRegistry ruleProviderRegistry) {
		this.ruleProviderRegistry = ruleProviderRegistry;
	}

	public Object[] getChildren() {
		if (ruleProviderRegistry == null) {
			return new Object[0];
		}
		return XMLRulesetModelUtil.readSystemRuleProviders(ruleProviderRegistry).stream().toArray(RuleProvider[]::new);
	}
	
	public static class SystemRulesNode extends RulesNode {
		public SystemRulesNode (RuleProviderRegistry registry) {
			super (registry);
		}
	}
	
	public static class CustomRulesNode extends RulesNode {
				
		private ModelService modelService;
		
		public CustomRulesNode (ModelService modelService) {
			super (null);
			this.modelService = modelService;
		}
		
		@Override
		public Object[] getChildren() {
			modelService.cleanPhantomCustomRuleProviders();
			return modelService.getModel().getCustomRuleRepositories().stream().toArray(CustomRuleProvider[]::new);
		}
	}
	
	public static class RulesetFileNode {
		
		private Object ruleProvider;
		private File file;
		private RuleProviderType type;

		public RulesetFileNode (Object ruleProvider, File file, RuleProviderType type) {
			this.ruleProvider = ruleProvider;
			this.file = file;
			this.type = type;
		}
		
		public Object getRuleProvider() {
			return ruleProvider;
		}
		
		public String getName() {
			return file.getName();
		}
		
		public File getFile() {
			return file;
		}
		
		public RuleProviderType getRuleProviderType () {
			return type;
		}
	}
}
