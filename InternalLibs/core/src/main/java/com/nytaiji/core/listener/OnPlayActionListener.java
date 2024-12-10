package com.nytaiji.core.listener;


import com.nytaiji.core.base.BaseConstants;

public interface OnPlayActionListener  {
	void start();
	void pause();
	void replay();
	void release();
	void onPositionChanged(long position);
	void onSpeedChanged(float speed);
	void onMuteChanged();
	void onScaleChanged(BaseConstants.ScaleType scaleType);
	void onIndexChanged(int vIndex);
}
