#Compass

The compass's purpose is to show where north is in the MapView. It always points toward the location of 0 degrees in the map.

Additionally, when the compass is tapped, it will re-orient the MapView back to north.

By default, the compass only displays when the MapView is not pointing north. This behavior can be changed using ```Compass.setAutoHide(boolean autoHide)``` 


##Default behavior
The Compass can be placed either in the XML layout, or programatically, and all layout and sizing can be handled by the application. When using this workflow, once the 
Compass is created, use ```bindTo(MapView)``` to attach the compass view to the MapView so that it can follow the map's orientation.

##Additional behavior
Control of layout and sizing can also be left to the Compass. If a compass is created programatically and added to the MapView using ```addToMapView(MapView)``` then layout and sizing will be handled by the Compass. It will be located in the top right of the mapview and will attempt to take up TBD% of the screen, depending on the size of the screen and the current size of the MapView and insets. 