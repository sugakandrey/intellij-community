package com.intellij.ide.actions;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ContextHelpAction extends AnAction {
  private static final Icon myIcon=IconLoader.getIcon("/actions/help.png");
  private final String myHelpID;

  public ContextHelpAction() {
    this(null);
  }

  public ContextHelpAction(@NonNls String helpID) {
    myHelpID = helpID;
  }

  public void actionPerformed(AnActionEvent e) {
    DataContext dataContext = e.getDataContext();
    final String helpId = getHelpId(dataContext);
    if (helpId != null) {
      HelpManager.getInstance().invokeHelp(helpId);
    }
  }

  @Nullable
  protected String getHelpId(DataContext dataContext) {
    if (myHelpID != null) {
      return myHelpID;
    }
    Object helpIDObj = dataContext.getData(DataConstants.HELP_ID);
    if (helpIDObj != null) {
      return helpIDObj.toString();
    }
    return null;
  }

  public void update(AnActionEvent event){
    Presentation presentation = event.getPresentation();
    if (ActionPlaces.MAIN_MENU.equals(event.getPlace())) {
      DataContext dataContext = event.getDataContext();
      presentation.setEnabled(getHelpId(dataContext) != null);
    }
    else {
      presentation.setIcon(myIcon);
      presentation.setText(CommonBundle.getHelpButtonText());
    }
  }
}
