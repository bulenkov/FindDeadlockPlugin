package com.intellij;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.Alarm;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Konstantin Bulenkov
 */
public class FindDeadlockAction extends AnAction {
    private boolean myRunning = false;
    private Alarm myEdtAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    @Override
    public void actionPerformed(AnActionEvent e) {
        myRunning = !myRunning;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Editor editor = getEditor();
                if (editor != null) {
                    invokeCompletion();

                    if (myRunning) {
                        myEdtAlarm.cancelAllRequests();
                        myEdtAlarm.addRequest(this, 300);
                    }
                }
            }
        };

        if (myRunning) {
            myEdtAlarm.addRequest(runnable, 300);
        } else {
            myEdtAlarm.cancelAllRequests();
        }
    }

    private void invokeCompletion() {
        try {
            HintManager.getInstance().hideAllHints();
            Robot robot = new Robot();
            robot.delay(100);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.delay(300);
        } catch (AWTException ignore) {
        }
    }

    @Override
    public void update(AnActionEvent e) {
        if (myRunning) {
            e.getPresentation().setText("Stop looking for deadlock");
        } else {
            e.getPresentation().setText("Start looking for deadlock");
        }
    }

    private Editor getEditor() {
        Component focusOwner = IdeFocusManager.findInstance().getFocusOwner();
        if (focusOwner instanceof EditorComponentImpl) {
            return ((EditorComponentImpl) focusOwner).getEditor();
        }
        return null;
    }
}
