package de.tum.www1.orion.tips;

import com.intellij.CommonBundle;
import com.intellij.ide.util.TipPanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class OrionTipsDialog extends DialogWrapper {
    private static OrionTipsDialog ourInstance;
    private TipPanel myTipPanel;

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    public OrionTipsDialog() {
        super(WindowManagerEx.getInstanceEx().findVisibleFrame(), true);
        initialize();
    }

    public OrionTipsDialog(@NotNull final Window parent) {
        super(parent, true);
        initialize();
    }

    private void initialize() {
        setModal(false);
        setTitle("Orion Tips And Tricks");
        setCancelButtonText(CommonBundle.getCloseButtonText());
        myTipPanel = new TipPanel();
        myTipPanel.setTips(new ArrayList<>(OrionTipAndTrickBean.EP_NAME.getExtensionList()));
        myTipPanel.nextTip();
        setDoNotAskOption(myTipPanel);
        setHorizontalStretch(1.33f);
        setVerticalStretch(1.25f);
        init();
    }

    @NotNull
    @Override
    protected DialogStyle getStyle() {
        return DialogStyle.COMPACT;
    }

    @Override
    protected JComponent createSouthPanel() {
        JComponent component = super.createSouthPanel();
        component.setBorder(JBUI.Borders.empty(8, 12));
        return component;
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        if (ApplicationManager.getApplication().isInternal()) {
            return new Action[]{new OrionTipsDialog.OpenTipsAction(), new OrionTipsDialog.PreviousTipAction(), new OrionTipsDialog.NextTipAction(), getCancelAction()};
        }
        return new Action[]{new OrionTipsDialog.PreviousTipAction(), new OrionTipsDialog.NextTipAction(), getCancelAction()};
    }

    @Override
    protected JComponent createCenterPanel() {
        return myTipPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public static void showForProject(@Nullable Project project) {
        createForProject(project);
        ourInstance.show();
    }

    /**
     * @deprecated Use {@link #showForProject(Project)} instead
     */
    @Deprecated
    public static OrionTipsDialog createForProject(@Nullable Project project) {
        Window w = WindowManagerEx.getInstanceEx().suggestParentWindow(project);
        if (ourInstance != null && ourInstance.isVisible()) {
            ourInstance.dispose();
        }
        return ourInstance = (w == null) ? new OrionTipsDialog() : new OrionTipsDialog(w);
    }

    public static void hideForProject(@Nullable Project project) {
        if (ourInstance != null) {
            ourInstance.dispose();
            ourInstance = null;
        }
    }

    private class OpenTipsAction extends AbstractAction {
        private static final String LAST_OPENED_TIP_PATH = "last.opened.tip.path";

        OpenTipsAction() {
            super("Open Orion Tips");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true)
                    .withFileFilter(file -> Comparing.equal(file.getExtension(), "html", SystemInfo.isFileSystemCaseSensitive));
//            String value = propertiesComponent.getValue(LAST_OPENED_TIP_PATH);
//            VirtualFile lastOpenedTip = value != null ? LocalFileSystem.getInstance().findFileByPath(value) : null;
            VirtualFile[] pathToSelect = VirtualFile.EMPTY_ARRAY; // lastOpenedTip != null ? new VirtualFile[]{lastOpenedTip} : VirtualFile.EMPTY_ARRAY;
            VirtualFile[] choose = FileChooserFactory.getInstance().createFileChooser(descriptor, null, myTipPanel).choose(null, pathToSelect);
            if (choose.length > 0) {
                ArrayList<OrionTipAndTrickBean> tips = new ArrayList<>();
                for (VirtualFile file : choose) {
                    OrionTipAndTrickBean tip = new OrionTipAndTrickBean();
                    tip.fileName = file.getPath();
                    tip.featureId = null;
                    tips.add(tip);
//                    propertiesComponent.setValue(LAST_OPENED_TIP_PATH, file.getPath());
                }
                myTipPanel.setTips(tips);
                myTipPanel.nextTip();
            }
        }
    }

    private class PreviousTipAction extends AbstractAction {
        PreviousTipAction() {
            super("Previous Orion Tip");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myTipPanel.prevTip();
        }
    }

    private class NextTipAction extends AbstractAction {
        NextTipAction() {
            super("Next Orion Tip");
            putValue(DialogWrapper.DEFAULT_ACTION, Boolean.TRUE);
            putValue(DialogWrapper.FOCUSED_ACTION, Boolean.TRUE); // myPreferredFocusedComponent
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myTipPanel.nextTip();
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myPreferredFocusedComponent;
    }
}
