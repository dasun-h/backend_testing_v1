@echo off

rem  ============================================
rem
rem  Used for printing console output messages
rem  mode (WinNT/2K/Win7 only)
rem
rem  ============================================

2>NUL CALL :CASE_%1 

EXIT /B

:CASE_test_start
	echo:
	echo ########################################## Starting Test ##########################################
	echo:
	GOTO END_CASE
:CASE_clean_local_artifacts
	echo:
	echo #################################### Cleaning Local Artifacts #####################################
	echo:
	GOTO END_CASE
:CASE_install_pageobjects
	echo:
	echo ################################# Installing PageObject Artifacts ##################################
	echo:
	GOTO END_CASE
:DEFAULT_CASE
	echo:
	echo Log Not Specified 
	echo:
	GOTO END_CASE
:END_CASE
  GOTO :EOF # return from CALL