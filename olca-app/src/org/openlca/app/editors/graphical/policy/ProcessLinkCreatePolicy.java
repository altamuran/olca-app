/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.app.editors.graphical.policy;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.openlca.app.editors.graphical.command.CommandFactory;
import org.openlca.app.editors.graphical.command.CreateLinkCommand;
import org.openlca.app.editors.graphical.model.ConnectionLink;
import org.openlca.app.editors.graphical.model.ExchangeNode;
import org.openlca.app.editors.graphical.model.ExchangePart;
import org.openlca.app.editors.graphical.model.ProcessNode;

public class ProcessLinkCreatePolicy extends GraphicalNodeEditPolicy {

	private PolylineConnection connection;

	@Override
	protected Connection createDummyConnection(Request req) {
		connection = (PolylineConnection) super.createDummyConnection(req);
		connection.setForegroundColor(ConnectionLink.COLOR);
		if (req instanceof CreateConnectionRequest) {
			CreateLinkCommand command = (CreateLinkCommand) ((CreateConnectionRequest) req)
					.getStartCommand();
			if (command.getSourceNode() != null)
				connection.setTargetDecoration(new PolygonDecoration());
			else if (command.getTargetNode() != null)
				connection.setSourceDecoration(new PolygonDecoration());
		} else
			connection.setTargetDecoration(new PolygonDecoration());
		return connection;
	}

	@Override
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {
		if (request.getStartCommand() != null) {
			CreateLinkCommand cmd = (CreateLinkCommand) request
					.getStartCommand();
			if (request.getTargetEditPart().getModel() instanceof ExchangeNode) {
				ExchangeNode target = (ExchangeNode) request
						.getTargetEditPart().getModel();
				ProcessNode targetNode = target.getParent().getParent();
				if (!target.getExchange().isInput())
					cmd.setSourceNode(targetNode);
				else if (!targetNode.getParent().hasConnection(cmd.getFlowId()))
					cmd.setTargetNode(targetNode);
				request.setStartCommand(cmd);
				return cmd;
			}
		}
		return null;
	}

	@Override
	protected Command getConnectionCreateCommand(
			final CreateConnectionRequest request) {
		CreateLinkCommand cmd = null;
		ExchangeNode target = (ExchangeNode) request.getTargetEditPart()
				.getModel();
		ProcessNode targetNode = target.getParent().getParent();
		long flowId = target.getExchange().getFlow().getId();
		if (!target.getExchange().isInput()) {
			cmd = CommandFactory.createCreateLinkCommand(flowId);
			cmd.setSourceNode(targetNode);
			request.setStartCommand(cmd);
		} else if (!targetNode.getParent().hasConnection(flowId)) {
			cmd = CommandFactory.createCreateLinkCommand(flowId);
			cmd.setTargetNode(targetNode);
			request.setStartCommand(cmd);
		}
		return cmd;
	}

	@Override
	protected ConnectionRouter getDummyConnectionRouter(
			CreateConnectionRequest request) {
		return ConnectionRouter.NULL;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		if (request.getTarget() instanceof ExchangePart) {
			ConnectionLink link = (ConnectionLink) request
					.getConnectionEditPart().getModel();
			ExchangeNode source = (ExchangeNode) request.getTarget().getModel();
			ProcessNode sourceNode = source.getParent().getParent();
			return CommandFactory.createReconnectLinkCommand(link, sourceNode,
					link.getTargetNode());
		}
		return null;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		if (request.getTarget() instanceof ExchangePart) {
			ConnectionLink link = (ConnectionLink) request
					.getConnectionEditPart().getModel();
			ExchangeNode target = (ExchangeNode) request.getTarget().getModel();
			ProcessNode targetNode = target.getParent().getParent();
			long flowId = link.getProcessLink().getFlowId();
			boolean canConnect = true;
			if (!link.getTargetNode().equals(targetNode)
					&& targetNode.getParent().hasConnection(flowId))
				canConnect = false;
			if (canConnect)
				return CommandFactory.createReconnectLinkCommand(link,
						link.getSourceNode(), targetNode);
		}
		return null;
	}

	@Override
	public void eraseSourceFeedback(final Request request) {
		// TODO adjust
		// if (getHost() instanceof ExchangePart) {
		// // unhighlight matching exchanges
		// ((ProductSystemNode) ((ExchangeNode) getHost().getModel())
		// .getParentProcessNode().getParent())
		// .unhighlightMatchingExchangeLabels();
		// ((ExchangeNode) getHost().getModel()).getFigure().setHighlight(
		// false);
		// }
		super.eraseSourceFeedback(request);
	}

	@Override
	public void showSourceFeedback(final Request request) {
		// TODO adjust
		// if (getHost() instanceof ExchangePart) {
		// // highlight matching exchanges
		// ((ProductSystemNode) ((ExchangeNode) getHost().getModel())
		// .getParentProcessNode().getParent())
		// .highlightMatchingExchangeLabels(selectedExchangeNode);
		// ((ExchangeNode) getHost().getModel()).getFigure()
		// .setHighlight(true);
		// }
		super.showSourceFeedback(request);
	}

}