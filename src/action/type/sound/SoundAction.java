package action.type.sound;

import GUI.soundpanel.SoundPanel;
import action.type.Action;

public abstract class SoundAction implements Action, Cloneable {

	public abstract void perform(SoundPanel p);

	public abstract void undo();

	@Override
	public SoundAction clone() throws CloneNotSupportedException {
		SoundAction cloneA = (SoundAction) super.clone();
		return cloneA;
	}

}