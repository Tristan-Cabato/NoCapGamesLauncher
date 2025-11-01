# Proxy Pattern completion
    - Added an "event listener" to load images when the panel is opened, otherwise destroy it.
    - I put it in quotations because its by no means an event listener, it's just a boolean check. ```if (currentPanel != null) {
            if (currentPanel instanceof Library) {
                ((Library) currentPanel).hidePanel();
            } else if (currentPanel instanceof Store) {
                ((Store) currentPanel).hidePanel();
            } else if (currentPanel == gameDetailPanel) {
                gameDetailPanel.setVisible(false);
            }
        }```
    - The friends panel is still a problem, I mean I guess we can just make them not have pictures - that way it won't be proxy, just JSON reading of all existing users and their owned games key.

# Game Detail overhaul
    - I might have spent more time here than I should have. I just reformatted it to show the photo, description, and made the button dynamic whether the user owns the game or not.

# Next Agenda
    - WE NEED TO GET THIS DONE BY TOMORROW HOLY SHIT

    Put ts in the end of the stack:
        - Sidebar Panel should look more like the first version
        - Searchbar integration
        - Code cleanup
        - JavaFX integration
        - Singleton fix


### Files Modified:
    >> Proxy Pattern complete <<
    /gui
        mainFrame.java
    /panels
        LoginForm.java
        Library.java
        Store.java
        GameDetail.java

    >> Game Detail Restructure | 1.0 <<
    /gui
        mainFrame.java
            - Handles what panel unloads (pause)
    /panels
        ↳ GameDetail.java
    
    >> Game Detail Restructure | 1.1 <<
    /gui
        mainFrame.java
    /components
        GameCardCreator.java
    /panels
        ↳ GameDetail.java
        Library.java
        Search.java
        Store.java

    >> Game Detail Restructure | 1.2 <<
    /userManager
        UserGameData.java
    /panels
        ↳ GameDetail.java
    Test.json

    >> Proxy and Iterator Patterns <<
    README.md
