---
trigger: manual
---

# Design Patterns
    **Creational Patterns:**
        - Prototype: To quickly clone a Game metadata object when creating multiple user-owned copies of the same game from the library, instead of querying the database each time.
        - Singleton: For the Library Manager or Installation Manager. This ensures there is a single point of control for installing, uninstalling, and launching games, managing shared resources and state.
    **Structural Patterns:**
        - Proxy: Use a Virtual Proxy to represent game icons or cover art. The high-resolution image is only loaded from the disk when it actually needs to be displayed in the UI, improving initial load performance.
        - Composite: To organize the game library. A GameCollection (composite) can contain individual Game objects (leafs) or other collections (e.g., "Favorites", "Completed", "Action Games"). This allows for recursive operations like "show total size of collection."
    **Behavioral Patterns:**
        - Iterator: To provide a standard way to traverse different collections of games (e.g., all games, filtered games, search results) without exposing the underlying data structure (e.g., list, set, tree).
        - Visitor: To define operations that can be performed on the game library without changing the Game or GameCollection classes (e.g., ExportToCSVVisitor, CheckForUpdatesVisitor, CalculateTotalSizeVisitor).

# Overall Idea
    - A user first has to register/login their account accordingly
    - There are local games pre-existing in the disk (WIP)
        - Said games are added to the library as a constant
    - A user may choose to "download" games from the store
        - These games then get added to their sidebar
    - Either from the sidebar or the library, on click the game should display its description
    - A user has the option to add or remove friends, and see their games
    - The users and supposedly the games are stored in a database

# Advices
    - Aside from adhering to the design patterns, the code must be maintainable, scalable, and encapsulated properly (in the case of duplicate codes)
    - Try not to scope out of the current interests, it may break everything in a domino effect

# TODO (This is for me, not the development flow)
    - Understand resourceLoader
    - Make all resourceManagers come together to begin integrating the database and structural patterns
