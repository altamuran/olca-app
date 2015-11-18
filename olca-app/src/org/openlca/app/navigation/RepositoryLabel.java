package org.openlca.app.navigation;

import org.eclipse.swt.graphics.Image;
import org.openlca.app.cloud.CloudUtil;
import org.openlca.app.cloud.index.Diff;
import org.openlca.app.cloud.index.DiffIndex;
import org.openlca.app.cloud.index.DiffIndexer;
import org.openlca.app.cloud.index.DiffType;
import org.openlca.app.db.Database;
import org.openlca.app.db.IDatabaseConfiguration;
import org.openlca.app.rcp.ImageManager;
import org.openlca.app.rcp.ImageType;
import org.openlca.app.util.Images;
import org.openlca.cloud.api.RepositoryClient;
import org.openlca.cloud.model.data.DatasetDescriptor;
import org.openlca.core.model.ModelType;

class RepositoryLabel {

	static final String CHANGED_STATE = "> ";

	static Image getWithOverlay(INavigationElement<?> element) {
		if (!Database.isConnected())
			return null;
		if (element instanceof DatabaseElement)
			return null;
		if (element instanceof GroupElement)
			return null;
		if (element instanceof ModelTypeElement)
			return null;
		DiffIndexer indexer = new DiffIndexer(Database.getDiffIndex());
		Diff diff = indexer.getDiff(CloudUtil
				.toDescriptor((INavigationElement<?>) element));
		if (diff.type != DiffType.NEW)
			return null;
		ImageType imageType = null;
		if (element instanceof CategoryElement)
			imageType = Images.getImageType(((CategoryElement) element)
					.getContent());
		else if (element instanceof ModelElement)
			imageType = Images.getImageType(((ModelElement) element)
					.getContent().getModelType());
		return ImageManager.getImageWithOverlay(imageType,
				ImageType.OVERLAY_ADDED);
	}

	static String getRepositoryText(IDatabaseConfiguration config) {
		if (!Database.isActive(config))
			return null;
		RepositoryClient client = Database.getRepositoryClient();
		if (client == null)
			return null;
		return " [" + client.getConfig().getServerUrl() + " "
				+ client.getConfig().getRepositoryId() + "]";
	}

	static String getStateIndicator(INavigationElement<?> element) {
		if (!Database.isConnected())
			return null;
		if (element instanceof NavigationRoot)
			return null;
		RepositoryClient client = Database.getRepositoryClient();
		if (client == null)
			return null;
		boolean hasChanged = hasChanged(element);
		if (!hasChanged)
			return null;
		if (element instanceof ModelElement && isNew(element))
			return null;
		return CHANGED_STATE;
	}

	private static boolean isNew(INavigationElement<?> element) {
		DiffIndexer indexer = new DiffIndexer(Database.getDiffIndex());
		Diff diff = indexer.getDiff(CloudUtil.toDescriptor(element));
		if (element instanceof DatabaseElement)
			return false;
		if (element instanceof GroupElement)
			return false;
		if (element instanceof ModelTypeElement)
			return false;
		return diff.type == DiffType.NEW;
	}

	private static boolean hasChanged(INavigationElement<?> element) {
		if (element instanceof CategoryElement)
			return hasChanged(CloudUtil.toDescriptor(element));
		if (element instanceof ModelElement)
			return hasChanged(CloudUtil.toDescriptor(element));
		if (element instanceof ModelTypeElement)
			return hasChanged(((ModelTypeElement) element).getContent());
		for (INavigationElement<?> child : element.getChildren())
			if (hasChanged(child))
				return true;
		return false;
	}

	private static boolean hasChanged(DatasetDescriptor descriptor) {
		DiffIndexer indexer = new DiffIndexer(Database.getDiffIndex());
		Diff diff = indexer.getDiff(descriptor);
		return diff.hasChanged() || diff.childrenHaveChanged();
	}

	private static boolean hasChanged(ModelType type) {
		DiffIndex index = Database.getDiffIndex();
		return index.hasChanged(type);
	}

}
