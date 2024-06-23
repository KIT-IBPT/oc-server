# Orbit Correction Server

OrbitCorrection server is based on Machine Physics calculation model written in Java. 
It uses linear beam optics to calculate necessary machine model parameters. 
It uses SVD to invert response matrix for orbit correction calculation.

OrbitCorrection server can work in "fast" mode in a way that tries to apply orbit correction as frequently as possible, wile doing reasonable compromise on precision. 
This enables OC to be run during energy ramping and during insertion device activation.

OrbitCorrection server runs embedded EPICS Application server, which exposes API to remote clients. 

OrbitCorrection server application has been developed for and it is used at [KARA](https://www.ibpt.kit.edu/kara.php) at [Institute of Beam Physics and Technology / KIT](https://www.ibpt.kit.edu/).

## Documentation

Development documentation can be found here: [https://kit-ibpt.github.io/oc-server/](https://kit-ibpt.github.io/oc-server/).

## Copyright / License

This project is licensed under the terms of the [GNU Lesser General Public License v3.0 license](LICENSE) by 
[Karlsruhe Institute of Technology's Institute of Beam Physics and Technology](https://www.ibpt.kit.edu/) 
and was developed by [igor@scictrl.com](http://scictrl.org).
