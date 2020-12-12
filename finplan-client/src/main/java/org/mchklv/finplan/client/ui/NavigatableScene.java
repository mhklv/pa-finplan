package org.mchklv.finplan.client.ui;

import java.util.HashMap;
import java.util.Stack;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.LocalStorageManager;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class NavigatableScene extends Scene {
    // private HashMap<String, Parent> rootsMap = new HashMap<String, Parent>();
    private Stack<Parent> rootsStack = new Stack<Parent>();

	public NavigatableScene(Parent root) {
		super(root);
        double primaryFontSizePx = DPICustomUtils
                .fromPtToPx(LocalStorageManager.getSettingsManager().getPrimaryFont().getSize());
        DPICustomUtils.setNodeFontSize(root, primaryFontSizePx);
        rootsStack.push(root);
	}

    public void setNewRoot(Parent newRoot) {
        rootsStack.push(newRoot);
        setRoot(newRoot);
        double primaryFontSizePx = DPICustomUtils
            .fromPtToPx(LocalStorageManager.getSettingsManager().getPrimaryFont().getSize());
        DPICustomUtils.setNodeFontSize(newRoot, primaryFontSizePx);
    }

    public void setPrevoiusRoot() {
        if (rootsStack.size() == 1) {
            throw new RuntimeException("Can't navigate to before the first root.");
        }
        else {
            rootsStack.pop();
            setRoot(rootsStack.peek());
        }
    }
}
