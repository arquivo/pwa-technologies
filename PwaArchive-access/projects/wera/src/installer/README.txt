NwaToolset installer building

In order to generate WERA packages you need to have AntInstaller, a 
front-end for Ant installed on the machine where you are building 
the installer package.

Pre-requisites:
Install Ant, Ant-contrib and AntInstaller
http://ant.apache.org/, http://ant-contrib.sourceforge.net/, http://antinstaller.sourceforge.net/,


1. Copy (or rename) the $ANTINSTALLERHOME/wera directory to 
   $ANTINSTALLERHOME/wera.

2. Copy ant-contrib.jar into $ANTINSTALLERHOME/wera/installlib

3. Download build-nwatoolset.xml, antinstall-config.xml and build.xml 
   from the installer directory at sourceforge and place them 
   in $ANTINSTALLERHOME/wera
   
4. cd to $ANTINSTALLERHOME/wera and execute ant -buildfile build-wera.xml

5. The installer package, wera-<tag or date>-installer.jar and 
   wera-<tag or date>-manual-install.tar.gz is created in $ANTINSTALLERHOME/wera

6. Installation testing: java -jar wera-<tag or date>-installer.jar