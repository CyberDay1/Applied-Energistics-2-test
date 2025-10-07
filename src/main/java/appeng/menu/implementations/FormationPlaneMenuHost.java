package appeng.menu.implementations;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IConfigInvHost;

/**
 * Shared host contract for the formation plane menu so both the part and block implementations can reuse the same UI.
 */
public interface FormationPlaneMenuHost extends IUpgradeableObject, IConfigInvHost, IConfigurableObject {
}

