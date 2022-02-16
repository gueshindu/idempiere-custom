@Title ... p2 director
@Echo on

cd %~dp0
set DESTINATION=%cd%
@echo %DESTINATION%


SET REPO=%1
SET FEATURE="com.gue.plugin.feature.group"

@echo %REPO% %FEATURE%

copy idempiere.ini idempiere.ini.sav

@FOR %%c in (plugins\org.eclipse.equinox.launcher_1.*.jar) DO set JARFILE=%%c
@echo JARFILE = %JARFILE%

java -Dosgi.noShutdown=false -Dosgi.compatibility.bootdelegation=true -Dosgi.install.area=director -jar %JARFILE% -application org.eclipse.equinox.p2.director -consoleLog -profileProperties org.eclipse.update.install.features=true -destination %DESTINATION% -repository %REPO% -u %FEATURE%

java -Dosgi.noShutdown=false -Dosgi.compatibility.bootdelegation=true -Dosgi.install.area=director -jar %JARFILE% -application org.eclipse.equinox.p2.director -consoleLog -profileProperties org.eclipse.update.install.features=true -destination %DESTINATION% -repository %REPO% -i %FEATURE%

copy idempiere.ini.sav idempiere.ini