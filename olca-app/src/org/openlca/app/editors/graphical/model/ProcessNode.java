/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.app.editors.graphical.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.openlca.app.db.Database;
import org.openlca.app.editors.graphical.layout.GraphLayoutManager;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import com.google.common.base.Objects;

public class ProcessNode extends Node {

	public static String CONNECTION = "Connection";

	private ProcessDescriptor process;
	private List<ConnectionLink> links = new ArrayList<>();
	private Rectangle xyLayoutConstraints;
	private boolean minimized = true;

	public ProcessNode(ProcessDescriptor process) {
		this.process = process;
	}

	@Override
	public ProductSystemNode getParent() {
		return (ProductSystemNode) super.getParent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<InputOutputNode> getChildren() {
		return (List<InputOutputNode>) super.getChildren();
	}

	private ProcessFigure getProcessFigure() {
		return (ProcessFigure) getFigure();
	}

	@Override
	protected void setFigure(IFigure figure) {
		xyLayoutConstraints = new Rectangle(0, 0, figure.getPreferredSize(-1,
				-1).width, figure.getPreferredSize(-1, -1).height);
		super.setFigure(figure);
	}

	public void add(ConnectionLink connectionLink) {
		links.add(connectionLink);
		getSupport().firePropertyChange(CONNECTION, null, connectionLink);
	}

	public void remove(ConnectionLink connectionLink) {
		links.remove(connectionLink);
		getSupport().firePropertyChange(CONNECTION, connectionLink, null);
	}

	public List<ConnectionLink> getLinks() {
		return links;
	}

	public void removeAllLinks() {
		for (ConnectionLink link : links)
			if (!link.getSourceNode().getFigure().isVisible()
					|| !link.getTargetNode().getFigure().isVisible())
				link.unlink();
	}

	@Override
	public String getName() {
		String text = process.getName();
		text += process.getLocation() != null ? " [" + process.getLocation()
				+ "]" : "";
		return text;
	}

	public ProcessDescriptor getProcess() {
		return process;
	}

	public boolean isMinimized() {
		return minimized;
	}

	public void minimize() {
		this.minimized = true;
		refresh();
	}

	public void maximize() {
		this.minimized = false;
		if (getChildren().isEmpty())
			initializeExchangeNodes();
		refresh();
	}

	private void initializeExchangeNodes() {
		Process process = new ProcessDao(Database.get()).getForId(this.process
				.getId());
		List<Exchange> technologies = new ArrayList<>();
		for (Exchange exchange : process.getExchanges())
			if (exchange.getFlow().getFlowType() == FlowType.ELEMENTARY_FLOW)
				continue;
			else
				technologies.add(exchange);
		Exchange[] technologyArray = technologies
				.toArray(new Exchange[technologies.size()]);
		add(new InputOutputNode(technologyArray));
	}

	private void refresh() {
		xyLayoutConstraints = new Rectangle(getProcessFigure().getLocation(),
				getFigure().getPreferredSize());
		getProcessFigure().refresh();
	}

	public ExchangeNode getExchangeNode(long flowId) {
		for (ExchangeNode node : getExchangeNodes())
			if (!node.isDummy())
				if (node.getExchange().getFlow().getId() == flowId)
					return node;
		return null;
	}

	public ExchangeNode[] getExchangeNodes() {
		List<ExchangeNode> exchangesNodes = new ArrayList<>();
		for (InputOutputNode node : getChildren())
			for (ExchangeNode node2 : node.getChildren())
				exchangesNodes.add(node2);
		ExchangeNode[] result = new ExchangeNode[exchangesNodes.size()];
		exchangesNodes.toArray(result);
		return result;
	}

	public Rectangle getXyLayoutConstraints() {
		return xyLayoutConstraints;
	}

	public void setXyLayoutConstraints(Rectangle xyLayoutConstraints) {
		this.xyLayoutConstraints = xyLayoutConstraints;
		getSupport().firePropertyChange(Node.PROPERTY_LAYOUT, null, "not null");
	}

	public int getMinimumHeight() {
		if (isMinimized())
			return ProcessFigure.MINIMUM_HEIGHT;
		return getProcessFigure().getMinimumHeight();
	}

	public int getMinimumWidth() {
		return ProcessFigure.MINIMUM_WIDTH;
	}

	public void setLinksHighlighted(boolean value) {
		for (ConnectionLink link : links)
			if (value)
				link.setHighlighted(1);
			else
				link.setHighlighted(0);
	}

	public boolean hasConnections() {
		if (links.size() > 0)
			return true;
		return false;
	}

	public void collapseLeft() {
		getProcessFigure().getLeftExpander().collapse();
	}

	public void collapseRight() {
		getProcessFigure().getRightExpander().collapse();
	}

	public void expandLeft() {
		getProcessFigure().getLeftExpander().expand();
	}

	public void expandRight() {
		getProcessFigure().getRightExpander().expand();
	}

	public boolean isExpandedLeft() {
		return getProcessFigure().getLeftExpander().isExpanded();
	}

	public boolean isExpandedRight() {
		return getProcessFigure().getRightExpander().isExpanded();
	}

	public void layout() {
		GraphLayoutManager layoutManager = (GraphLayoutManager) getParent()
				.getFigure().getLayoutManager();
		layoutManager.layout(getFigure(), getParent().getEditor()
				.getLayoutType());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProcessNode))
			return false;
		ProcessNode other = (ProcessNode) obj;
		return Objects.equal(getProcess(), other.getProcess());
	}

}