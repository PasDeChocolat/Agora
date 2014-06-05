# Unlimited Undo: Space as a Value

Application programming interfaces (APIs) allow software developers to access external resources for their own applications. This project presents the kernel of a 'physical API' to allow other artists & visitors to change the state of the Kaka‘ako Agora.

## Kaka‘ako Agora

*Kaka‘ako Agora* [(on Kickstarter)](https://www.kickstarter.com/projects/1872441385/kakaako-agora-an-indoor-public-park-by-atelier-bow) is a park designed for 21st Century Honolulu. It is the first project of it's kind in Honolulu: A beautiful indoor pavilion that calls on world class architecture to create a dynamic space for our local community.

### The Reusable Framework

This project provides an API for saving and replaying data stored by artists, which represents activity within the Agora. Using this framework allows artists to concentrate on the input and output respresentation of this data, rather than the sometimes complicated work or data input, storage and output.

The framework makes use of the following technologies:
* Datomic database
* ClojureScript Om
* Facebook's React

Giving it the following novel properties:
* Timetravel-like undo capabilities
* Effortless query and playback of historical data
