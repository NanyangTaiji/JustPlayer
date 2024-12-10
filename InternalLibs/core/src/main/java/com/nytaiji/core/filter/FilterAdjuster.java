package com.nytaiji.core.filter;

import com.nytaiji.epf.filter.GlFilter;

public interface FilterAdjuster {
    void adjust(GlFilter filter, int percentage);
}
