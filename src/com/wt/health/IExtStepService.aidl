package com.wt.health;

import com.wt.health.IExtStepServiceCB;

interface IExtStepService{
	void registerExtCB(in IExtStepServiceCB cb);
}