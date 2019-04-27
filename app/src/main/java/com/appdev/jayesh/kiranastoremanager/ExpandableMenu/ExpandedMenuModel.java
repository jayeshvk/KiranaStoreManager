package com.appdev.jayesh.kiranastoremanager.ExpandableMenu;

public class ExpandedMenuModel {

    String mainMenu = "";
    int iconImg = -1; // menu icon resource id

    public ExpandedMenuModel() {
    }

    public ExpandedMenuModel(String mainMenu, int iconImg, boolean hasChildren) {
        this.mainMenu = mainMenu;
        this.iconImg = iconImg;
        this.hasChildren = hasChildren;
    }


    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    boolean hasChildren;

    public String getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(String mainMenu) {
        this.mainMenu = mainMenu;
    }

    public int getIconImg() {
        return iconImg;
    }

    public void setIconImg(int iconImg) {
        this.iconImg = iconImg;
    }
}