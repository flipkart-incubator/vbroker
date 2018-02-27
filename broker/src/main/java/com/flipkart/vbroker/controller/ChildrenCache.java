package com.flipkart.vbroker.controller;

import java.util.ArrayList;
import java.util.List;

public class ChildrenCache {

    private List<String> children;

    public ChildrenCache(List<String> children) {
        this.children = children;
    }

    public List<String> getChildren() {
        return this.children;
    }

    /**
     * Returns the diff b/w new and existing set of elements. Also, sets the new
     * set as children.
     *
     * @param newChildren
     * @return
     */
    public List<String> diffAndSet(List<String> newChildren) {
        List<String> diff = new ArrayList<String>();
        newChildren.forEach(child -> {
            if (!children.contains(child)) {
                diff.add(child);
            }
        });
        this.children = newChildren;
        return diff;
    }
}
