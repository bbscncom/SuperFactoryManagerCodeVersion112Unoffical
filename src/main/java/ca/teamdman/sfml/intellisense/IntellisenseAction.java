package ca.teamdman.sfml.intellisense;

import ca.teamdman.sfm.client.gui.widget.PickListItem;
import ca.teamdman.sfml.manipulation.ManipulationResult;

public interface IntellisenseAction extends PickListItem {
    ManipulationResult perform(IntellisenseContext context);
}
