/*******************************************************************************
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.windup.ui.internal.rules.delegate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.jboss.tools.windup.ui.internal.Messages;
import org.jboss.tools.windup.ui.internal.editor.AddNodeAction;
import org.jboss.tools.windup.ui.internal.editor.ElementAttributesContainer;
import org.jboss.tools.windup.ui.internal.editor.RulesetElementUiDelegateFactory.NodeRow;
import org.jboss.tools.windup.ui.internal.editor.RulesetElementUiDelegateFactory.RulesetConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

@SuppressWarnings("restriction")
public class RuleDelegate extends ElementUiDelegate {
	
	private static class BooleanAttributeRow extends NodeRow {
		
		private Button button;

		public BooleanAttributeRow(Node parent, CMNode cmNode) {
			super(parent, cmNode);
		}

		@Override
		public void createContents(Composite parent, FormToolkit toolkit, int span) {
			button = toolkit.createButton(parent, Messages.taskRule, SWT.CHECK);
			GridDataFactory.fillDefaults().grab(true, false).span(span, 1).applyTo(button);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!blockNotification) {
						setValue(button.getSelection());
					}
				}
			});
		}
		
		protected void setValue(boolean value) {
		}
		
		@Override
		protected void update() {
			boolean selected = false;
			String value = super.getValue();
			if (value != null && !value.isEmpty()) {
				selected = Boolean.valueOf(value);
			}
			if (button.getSelection() != selected) {
				button.setSelection(selected);
			}
		}
		
		@Override
		public void setFocus() {
		}
	}
	
	@Override
	protected void createTabs() {
		addTab(DetailsTab.class);
	}
	
	public static class DetailsTab extends ElementAttributesContainer {
		
		private BooleanAttributeRow typeRow;
		
		private Composite statusParent;
		private Composite commentsParent;

		private RuleStatusSection statusSection;
		private TaskRuleComments commentsSection;
		
		private Composite parent;
		private Composite mainDetailsContainer;
		
		@PostConstruct
		public void createControls(Composite parent) {
			this.parent = parent;
			this.mainDetailsContainer = toolkit.createComposite(parent);
			
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			layout.makeColumnsEqualWidth = true;
			mainDetailsContainer.setLayout(layout);
		
			GridDataFactory.fillDefaults().grab(true, false).applyTo(mainDetailsContainer);
			
			Composite detailsContainer = toolkit.createComposite(mainDetailsContainer);
			
			GridLayout detailsContainerLayout = new GridLayout();
			detailsContainerLayout.marginHeight = 0;
			detailsContainerLayout.marginWidth = 0;
			detailsContainer.setLayout(detailsContainerLayout);
		
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(detailsContainer);
			
			Composite client = super.createSection(detailsContainer, 2, toolkit, element, ExpandableComposite.TITLE_BAR |Section.NO_TITLE_FOCUS_BOX, 
					null, null);
			
			GridLayout glayout = FormLayoutFactory.createSectionClientGridLayout(false, 2);
			glayout.marginTop = 0;
			glayout.marginBottom = 0;
			client.setLayout(glayout);
			
			CMElementDeclaration ed = modelQuery.getCMElementDeclaration(element);
			if (ed != null) {
				CMAttributeDeclaration declaration = findDeclaration(RulesetConstants.ID);
				rows.add(ElementAttributesContainer.createTextAttributeRow(element, toolkit, declaration, client, 2));
			}
			((Section)client.getParent()).setExpanded(true);
			
			//createTaskArea(client); until 4.1.0
			//createStack(mainDetailsContainer);
		}
		
		@SuppressWarnings("unchecked")
		private CMAttributeDeclaration findDeclaration(String name) {
			CMElementDeclaration ed = modelQuery.getCMElementDeclaration(element);
			List<CMAttributeDeclaration> availableAttributeList = modelQuery.getAvailableContent(element, ed, ModelQuery.INCLUDE_ATTRIBUTES);
			Optional<CMAttributeDeclaration> option = availableAttributeList.stream().filter(e -> Objects.equal(e.getAttrName(), name)).findFirst();
			if (option.isPresent()) {
				return option.get();
			}
			return null;
		}
		
		private void createTaskArea(Composite parent) {
			CMAttributeDeclaration declaration = findDeclaration(RulesetConstants.IS_TASK);
	    		typeRow = new BooleanAttributeRow(element, declaration) {
	    			@Override
	    			protected void setValue(boolean value) {
    					try {
    						model.aboutToChangeModel();
    						Node node = getNode();
    						if (node != null) {
    							contentHelper.setNodeValue(node, String.valueOf(value));
    						}
    						else {
    							AddNodeAction newNodeAction = new AddNodeAction(model, cmNode, parent, parent.getChildNodes().getLength());
    							newNodeAction.runWithoutTransaction();
    							if (!newNodeAction.getResult().isEmpty()) {
    								node = (Node)newNodeAction.getResult().get(0);
    								contentHelper.setNodeValue(node, String.valueOf(value));
    							}
    						}
    						Element task = findTaskElement();
	    					if (task != null) {
		    					element.removeChild(task);
		    				}
		    				if (value) {
		    					createTaskElement();
		    				}
    					}
    					finally {
    						model.changedModel();
    					}
	    			}
	    		};
	    		rows.add(typeRow);
	    		typeRow.createContents(parent, toolkit, 2);
		}
		
		private void createStatusArea(Composite parent) {
			IEclipseContext ctx = context.createChild();
			ctx.set(Composite.class, parent);
			statusSection = ContextInjectionFactory.make(RuleStatusSection.class, ctx);
		}
		
		private boolean isTaskType() {
			if (typeRow != null) {
				String value = typeRow.getValue();
				if (value != null && !value.isEmpty()) {
					return Boolean.valueOf(value);
				}
			}
			return false;
		}
		
//		private void createStack(Composite parent) {
//			this.stackComposite = toolkit.createComposite(parent);
//			stackComposite.setLayout(new StackLayout());
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(stackComposite);
//			placeholder = toolkit.createComposite(stackComposite);
//			GridLayoutFactory.fillDefaults().applyTo(placeholder);
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(placeholder);
//		}
		
		private void createTaskDetails() {
			statusParent = toolkit.createComposite(mainDetailsContainer);
			GridLayoutFactory.fillDefaults().applyTo(statusParent);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(statusParent);
			
			createStatusArea(statusParent);
			
			commentsParent = toolkit.createComposite(this.parent);
			GridLayoutFactory.fillDefaults().applyTo(commentsParent);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(commentsParent);
			
			commentsSection = createCommentsArea(commentsParent);
		}
		
		private TaskRuleComments createCommentsArea(Composite parent) {
			Element task = findTaskElement();
			CMElementDeclaration dec = modelQuery.getCMElementDeclaration(task);
			IEclipseContext commentContext = context.createChild();
			commentContext.set(Composite.class, parent);
			commentContext.set(Element.class, task);
			commentContext.set(CMElementDeclaration.class, dec);
			return ContextInjectionFactory.make(TaskRuleComments.class, commentContext);
		}
		
		private Element findTaskElement() {
			NodeList list = element.getElementsByTagName(RulesetConstants.TASK);
			if (list.getLength() > 0) {
				return (Element)list.item(0);
			}
			return null;
		}
		
		private void createTaskElement() {
			Element task = element.getOwnerDocument().createElement(RulesetConstants.TASK);
			element.appendChild(task);
		}
		
		private void updateStack() {
			GridData layoutData = (GridData)mainDetailsContainer.getChildren()[0].getLayoutData();
			Element taskElement = findTaskElement();
			boolean isTaskType = isTaskType();
			if (isTaskType && taskElement != null) {
				if (commentsSection != null && !commentsSection.isContainerFor(taskElement)) {
					statusParent.dispose();
					commentsParent.dispose();
					commentsSection = null;
				}
				if (commentsSection == null) {
					createTaskDetails();
				}
				statusSection.update();
				commentsSection.update();
				layoutData.horizontalSpan = 1;
			}
			else if (commentsSection != null){
				statusParent.dispose();
				commentsParent.dispose();
				commentsSection = null;
				layoutData.horizontalSpan = 2;
			}
			parent.layout(true, true);	
		}
		
		@Override
		protected void bind() {
			super.bind();
			updateStack();
		}
	}
	
	@Override
	public Object[] getChildren() {
		Object[] children = super.getChildren();
		List<Object> filtered = Arrays.stream(children).filter(child -> {
			if (Objects.equal(((Node)child).getNodeName(), RulesetConstants.TASK)) {
				return false;
			}
			return true;
		}).collect(Collectors.toList());
		return filtered.toArray(new Object[filtered.size()]);
	}
}