package org.futurerobotics.bluejay.original.localizers;

import android.support.annotation.Nullable;

public interface Localizer {
	@Nullable
	PoseOrientation getPosition();
}
