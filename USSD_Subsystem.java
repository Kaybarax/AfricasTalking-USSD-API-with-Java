/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * USSD and Airtime classy class with AfricasTalking API by, you know who!!
 * @author kevinbarasa
 */
@WebServlet(name = "ProcessUSSD_Servlet", urlPatterns = {"/ussd/processUSSD"})
public class ProcessUSSD_Servlet extends HttpServlet {

    private static final long serialVersionUID = -2543066334451132153L;
    
    private String sessionId, phoneNumber, serviceCode, text, textReference, response;
    private String currentSelection, previousSelection, languageSelection, welcomeMsg, updatedOriginPage, originPage;
    private String privilegedPollersList;
    
    //pages
    private final String page_welcome = "___page_welcome";
    private final String page_selectAge = "___selectAgePage";
    private final String page_selectGender="___page_selectGender";
    private final String page_selectEducation="___page_selectEducation";
    private final String page_selectEmployment="___page_selectEmployment";
    private final String page_selectAPollingCategory="___page_selectAPollingCategory";
    private final String page_pickYourPreferedPreziCandidate="___page_pickYourPreferedPreziCandidate";
    private final String page_searchConstit="___page_searchConstit";
    private final String page_confirmConstit="___page_confirmConstit";
    private final String page_constitNotFound="___page_constitNotFound";
    private final String page_pickYourPreferedMP="___page_pickYourPreferedMP";
    private final String page_searchCountySen="___page_searchCountySen";
    private final String page_confirmCountySens="___page_confirmCountySens";
    private final String page_countyNotFoundSens="___page_countyNotFoundSens";
    private final String page_pickYourPreferedSenator="___page_pickYourPreferedSenator";
    private final String page_searchCountyGov="___page_searchCountyGov";
    private final String page_confirmCountyGovs="___page_confirmCountyGovs";
    private final String page_countyNotFoundGovs="___page_countyNotFoundGovs";
    private final String page_pickYourPreferedGovernor="___page_pickYourPreferedGovernor";
    private final String page_searchCountyWRep="___page_searchCountyWRep";
    private final String page_confirmCountyWreps="___page_confirmCountyWreps";
    private final String page_countyNotFoundWReps="___page_countyNotFoundWReps";
    private final String page_pickYourPreferedWomenRep="___page_pickYourPreferedWomenRep";
    private final String page_searchWard = "___page_searchWard";
    private final String page_confirmWard = "___page_confirmWard";
    private final String page_wardNotFound = "___page_wardNotFound";
    private final String page_pickYourPreferedMCA = "___page_pickYourPreferedMCA";
    private final String page_ERROR = "___page_ERROR";
    
    private final String UNKNOWN_REPLY = "CON UNKNOWN REPLY!! / JIBU LAKO HALIELEWEKI!!\n"
            + "Try again / Jaribu tena: \n"
            + "1. English \n"
            + "2. Swahili \n";
    
    private String mySQLdriver, dbUrl, dbUser, dbUserPword;
    @SuppressWarnings("FieldMayBeFinal")
    private boolean dBDriverLoaded = true;
    @SuppressWarnings("FieldMayBeFinal")
    private boolean dBConnectionSuccess = true;
    @SuppressWarnings("FieldMayBeFinal")
    private boolean awardedPollerAirtime = false;
    @SuppressWarnings("FieldMayBeFinal")
    private boolean pollerCanAccessHere = false;
    
    private Connection conn=null;
    private ResultSet rs=null;
    private Statement s=null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/plain;charset=UTF-8");
        
        ServletContext sc = getServletContext();
        
        //Initialize
        this.sessionId = "";
        this.phoneNumber = "";
        this.serviceCode = ""; 
        this.text = "";
        this.textReference = "";
        this.response = "";
        this.welcomeMsg = "";
        this.currentSelection = "";
        this.previousSelection = "";
        this.languageSelection = "";
        this.originPage = "";
        this.privilegedPollersList = "";
        this.updatedOriginPage = "";
        this.awardedPollerAirtime = false;
        this.pollerCanAccessHere = false;
        
        //START
        this.response = this.UNKNOWN_REPLY;
        this.sessionId = request.getParameter("sessionId");
        this.phoneNumber = request.getParameter("phoneNumber");
        this.serviceCode = request.getParameter("serviceCode");
        this.text = request.getParameter("text");
        this.textReference = this.text;
        this.privilegedPollersList = (String)sc.getInitParameter("isPrivileged");
        
            
        try (PrintWriter out = response.getWriter()) {
            
            if (this.text.equals("")) {
                prepareWelcomeMessage();
                setCurrentSessionPage(sc);
                out.print(this.response);
            }
            else {
                this.originPage = (String)sc.getAttribute(this.sessionId);
                
                if (this.originPage.equals(this.page_welcome)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            loadAndConnectToDb(sc);
                            // before poller can continue into the system to use the
                            //system, all required details must be provided
                            if (!systemHasPollerBasicData()) {
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    continueInEnglish();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            loadAndConnectToDb(sc);
                            // before poller can continue into the system to use the
                            //system, all required details must be provided
                            if (!systemHasPollerBasicData()) {
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    continueInSwahili();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        default:
                            // deafault crash to avoid break during polling
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_selectAge)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    if (!this.currentSelection.equals("1") && 
                            !this.currentSelection.equals("2") && 
                            !this.currentSelection.equals("3") && 
                            !this.currentSelection.equals("4") && 
                            !this.currentSelection.equals("5")) {
                        backToTheBegining(sc, out);
                        return;
                    }
                    loadAndConnectToDb(sc);
                    if(addedPollerAge()) {
                        setCurrentSessionPage(sc); // update session
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            backToTheBegining(sc, out);
                            releaseResources();
                            return;
                        }
                    }
                    else {
                        releaseResources();
                        out.print(this.response);
                        return;
                    }
                }
                
                if (this.originPage.equals(this.page_selectGender)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    if (!this.currentSelection.equals("1") && 
                            !this.currentSelection.equals("2")) {
                        backToTheBegining(sc, out);
                        return;
                    }
                    loadAndConnectToDb(sc);
                    if(addedPollerGender()) {
                        setCurrentSessionPage(sc); // update session
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            backToTheBegining(sc, out);
                            releaseResources();
                            return;
                        }
                    }
                    else {
                        releaseResources();
                        out.print(this.response);
                        return;
                    }
                }
                
                if (this.originPage.equals(this.page_selectEducation)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    if (!this.currentSelection.equals("1") && 
                            !this.currentSelection.equals("2") && 
                            !this.currentSelection.equals("3") && 
                            !this.currentSelection.equals("4")) {
                        backToTheBegining(sc, out);
                        return;
                    }
                    loadAndConnectToDb(sc);
                    if(addedPollerEducationLevel()) {
                        setCurrentSessionPage(sc); // update session
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            backToTheBegining(sc, out);
                            releaseResources();
                            return;
                        }
                    }
                    else {
                        releaseResources();
                        out.print(this.response);
                        return;
                    }
                }
                
                if (this.originPage.equals(this.page_selectEmployment)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    if (!this.currentSelection.equals("1") && 
                            !this.currentSelection.equals("2") && 
                            !this.currentSelection.equals("3")) {
                        backToTheBegining(sc, out);
                        return;
                    }
                    loadAndConnectToDb(sc);
                    if(addedPollerEmploymentStatus()) {
                        setCurrentSessionPage(sc); // update session
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            backToTheBegining(sc, out);
                            releaseResources();
                            return;
                        }
                    }
                    else {
                        releaseResources();
                        out.print(this.response);
                        return;
                    }
                }
                
                if (this.originPage.equals(this.page_selectAPollingCategory)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    selectPreziPoll();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    if (this.languageSelection.equals("1")) {
                                        searchForAConstituencyEnglish();
                                    }
                                    if (this.languageSelection.equals("2")) {
                                        searchForAConstituencySwahili();
                                    }
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    searchForACountyWRep();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "4":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    searchForACountySen();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "5":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    searchForACountyGov();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "6":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    searchForAWard();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedPreziCandidate)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            preziWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                    
                }
                
                // mps final run
                
                if (this.originPage.equals(this.page_searchConstit)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            confirmSearchedConstituency();
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                if (this.originPage.equals(this.page_confirmConstit)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    getMPsOfSelectedConstituency();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    if (this.languageSelection.equals("1")) {
                                        searchForAConstituencyEnglish();
                                    }
                                    if (this.languageSelection.equals("2")) {
                                        searchForAConstituencySwahili();
                                    }
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_constitNotFound)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    if (this.languageSelection.equals("1")) {
                                        searchForAConstituencyEnglish();
                                    }
                                    if (this.languageSelection.equals("2")) {
                                        searchForAConstituencySwahili();
                                    }
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedMP)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            mpWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                //------------------------------------------------------------------
                
                // sens final run
                
                if (this.originPage.equals(this.page_searchCountySen)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.confirmSearchedCounty_Sen();
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                if (this.originPage.equals(this.page_confirmCountySens)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.getSenatorsOfSelectedCounty();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountySen();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_countyNotFoundSens)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountySen();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedSenator)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.senatorWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                //.....................................................................
                
                // govs final run
                
                if (this.originPage.equals(this.page_searchCountyGov)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.confirmSearchedCounty_Gv();
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                if (this.originPage.equals(this.page_confirmCountyGovs)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.getGovernorsOfSelectedCounty();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountyGov();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_countyNotFoundGovs)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountyGov();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedGovernor)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.governorWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                //.........................................................
                
                // women reps final run
                
                if (this.originPage.equals(this.page_searchCountyWRep)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.confirmSearchedCounty_WR();
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                if (this.originPage.equals(this.page_confirmCountyWreps)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.getWomenRepsOfSelectedCounty();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountyWRep();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_countyNotFoundWReps)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForACountyWRep();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedWomenRep)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.womenRepWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                // .......................................................
                
                // mcas final run
                
                if (this.originPage.equals(this.page_searchWard)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.confirmSearchedWard();
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                if (this.originPage.equals(this.page_confirmWard)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.getMCAsOfSelectedWard();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForAWard();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "3":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_wardNotFound)) {
                    switch (getCurrentTextSelection()) {
                        case "1":
                            setCurrentLanguageSelection(); // almost always set language first
                            loadAndConnectToDb(sc);
                            if (!systemHasPollerBasicData()) {
                                setCurrentSessionPage(sc); // update session
                                getPollerBasicData();
                                setCurrentSessionPage(sc); // update session
                                releaseResources();
                                out.print(this.response);
                                return;
                            } else {
                                if (!systemHasPollerFullDetails()) {
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                } else {
                                    this.pollerCanAccessHere = true;
                                    this.searchForAWard();
                                    setCurrentSessionPage(sc); // update session
                                    releaseResources();
                                    out.print(this.response);
                                    return;
                                }
                            }
                        case "2":
                            backToTheBegining(sc, out);
                            break;
                        default:
                            backToTheBegining(sc, out);
                            break;
                    }
                    return;
                }
                
                if (this.originPage.equals(this.page_pickYourPreferedMCA)) {
                    setCurrentLanguageSelection(); // almost always set language first
                    loadAndConnectToDb(sc);
                    if (!systemHasPollerBasicData()) {
                        setCurrentSessionPage(sc); // update session
                        getPollerBasicData();
                        setCurrentSessionPage(sc); // update session
                        releaseResources();
                        out.print(this.response);
                        return;
                    } else {
                        if (!systemHasPollerFullDetails()) {
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        } else {
                            this.pollerCanAccessHere = true;
                            this.mcaWeeklyPoll(sc);
                            setCurrentSessionPage(sc); // update session
                            releaseResources();
                            out.print(this.response);
                            return;
                        }
                    }
                }
                
                // .....................................................
                
/******************************* WHAT ABOUT MIMI!! WHOCAN CHANGE THE WORLD ************************************/
                    
		releaseResources();
            }
        }
        
        finally 
        {   
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="db instantiation, load and connection">
    /**
     * Instantiate db connection values, load and connect to db
     * @param sc 
     */
    @SuppressWarnings("UseSpecificCatch")
    private void loadAndConnectToDb(ServletContext sc)
    {
        //Get db connection values
        this.mySQLdriver = (String)sc.getInitParameter("mysqlDbDriver");
        this.dbUrl = (String)sc.getInitParameter("mysqlMainDbURL");
        this.dbUser = (String)sc.getInitParameter("mysqlMainDbUser");
        this.dbUserPword = (String)sc.getInitParameter("mysqlMainDbUserPword");
        
        //Load driver
        try
        {
            Class.forName(mySQLdriver).newInstance();
        }
        catch(Exception e)
        {
            this.dBDriverLoaded = false;
        }
        
        //connect to db
        try
        {
            conn = DriverManager.getConnection(dbUrl,dbUser,dbUserPword);
        }
        catch (Exception e)
        {
            dBConnectionSuccess = false;
        }
    } // </editor-fold>
    
    /**
     * Gracefully restarts you on occurrence of an error 
     * @param sc
     * @param out 
     */
    private void backToTheBegining(ServletContext sc, PrintWriter out) {
        setCurrentLanguageSelection();
        if (this.languageSelection.equals("1")) {
            loadAndConnectToDb(sc);
            // before poller can continue into the system to use the 
            //system, all required details must be provided 
            if (!systemHasPollerBasicData()) {
                getPollerBasicData();
                setCurrentSessionPage(sc); // update session
                releaseResources();
                out.print(this.response);
                return;
            } else {
                if (!systemHasPollerFullDetails()) {
                    setCurrentSessionPage(sc); // update session
                    releaseResources();
                    out.print(this.response);
                    return;
                } else {
                    this.pollerCanAccessHere = true;
                    continueInEnglish();
                    setCurrentSessionPage(sc); // update session
                    releaseResources();
                    out.print(this.response);
                    return;
                }
            }
        }
        if (this.languageSelection.equals("2")) {
            loadAndConnectToDb(sc);
            // before poller can continue into the system to use the 
            //system, all required details must be provided 
            if (!systemHasPollerBasicData()) {
                getPollerBasicData();
                setCurrentSessionPage(sc); // update session
                releaseResources();
                out.print(this.response);
            } else {
                if (!systemHasPollerFullDetails()) {
                    setCurrentSessionPage(sc); // update session
                    releaseResources();
                    out.print(this.response);
                } else {
                    this.pollerCanAccessHere = true;
                    continueInSwahili();
                    setCurrentSessionPage(sc); // update session
                    releaseResources();
                    out.print(this.response);
                }
            }
        }
    }
    
    private void setCurrentSessionPage(ServletContext sc) {
        sc.setAttribute(this.sessionId, this.updatedOriginPage);
//        if (newPoller()) {
//            createNewPollerSession();
//        }
//        else {
//            updateReturningPollerSession();
//        }
    }
    
    /**
     * prepareWelcomeMessage: Self described
     */
    private void prepareWelcomeMessage() {
        this.response = ""; //always
        this.welcomeMsg = "CON Welcome / Karibu, Kenya 2017 Elections Polling: \n";
        this.welcomeMsg += "Choose Language / Chagua Lugha \n";
        this.welcomeMsg += "1. English \n";
        this.welcomeMsg += "2. Swahili \n";
        this.response = this.welcomeMsg;
        
        //set session page
        this.updatedOriginPage = this.page_welcome;
    }
    
    /**
     * On selection to continue USSD session in English
     */
    private void continueInEnglish() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        this.response = ""; //always
        this.response = "CON Select a Polling Category: \n";
        this.response += "1. Presidential Candidates \n";
        this.response += "2. MPs \n";
        this.response += "3. Women Reps \n";
        this.response += "4. Senators \n";
        this.response += "5. Governors \n"
                + "6. MCAs \n";
        
        //set session page
        this.updatedOriginPage = this.page_selectAPollingCategory;
    }
    
    /**
     * On selection to continue USSD session in swahili
     */
    private void continueInSwahili() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        this.response = ""; //always
        this.response = "CON Chagua Kiwango Cha Uchaguzi: \n";
        this.response += "1. Wagombeaji Urais \n";
        this.response += "2. Wagombeaji Bunge \n";
        this.response += "3. Wawakilishi wa Wanawake \n";
        this.response += "4. Wagombeaji Seneti \n";
        this.response += "5. Wagombeaji Ugavana \n"
                + "6. MCAs \n";
        
        //set session page
        this.updatedOriginPage = this.page_selectAPollingCategory;
    }
    
    // <editor-fold defaultstate="collapsed" desc="PREZI POLLING">
    
    /**
     * USSD session selection for a presidential level poll
     */
    private void selectPreziPoll() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.";
                return;
            }
            return;
        }
        if(preziWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one poll per week per category. \n"
                        + "Select another category.\n";
                this.response += "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu, katika kila kiwango, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "2. Wagombeaji Bunge \n";
                this.response += "3. Wawakilishi wa Wanawake \n";
                this.response += "4. Wagombeaji Seneti \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        if (this.languageSelection.equals("1")) {
            this.response = ""; //always
            this.response = "CON Who is your prefered presidential candidate? \n";
            this.response += "REPLY WITH NUMBER ONLY, e.g. 1: \n";
        } else if (this.languageSelection.equals("2")) {
            this.response = ""; //always
            this.response = "CON Chagua mgombeaji urais unaemfaa: \n";
            this.response += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
        }
        
        int counter = 1;
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT JSON_EXTRACT(president_details, '$.preziName') "
                    + "FROM PresidentialCandidates");
            while(rs.next()) {
                this.response += counter++ /*+rs.getString(1)*/+". "+rs.getString(1)+".\n";
            }
            this.response = this.response.replace("\"", "");
            this.updatedOriginPage = this.page_pickYourPreferedPreziCandidate;
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Cannot get the candidates at this time.\n"
                + "Try again: \n"
                + "1. Continue \n";
                this.updatedOriginPage = this.page_ERROR;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kukamilisha wagombeaji urais.\n"
                + "Jaribu tena: \n"
                + "2. Endelea \n";
                this.updatedOriginPage = this.page_ERROR;
                return;
            }
            this.updatedOriginPage = this.page_ERROR;
        }
    }
    
    /**
     * USSD session action in polling for a selected presidential candidate  
     * @param sc 
     */
    private void preziWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(preziWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one poll per week for presidential candidates. \n"
                        + "Select another category.\n";
                this.response += "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji urais ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "2. Wagombeaji Bunge \n";
                this.response += "3. Wawakilishi wa Wanawake \n";
                this.response += "4. Wagombeaji Seneti \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String preziRefIID = "";
        boolean correctCandidateID = false;
        int id = 0;
        
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid Presidential Candidate ID.\n"
                        + "Please try again. \n"
                        + "1. Presidential Candidates \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "1. Wagombeaji Urais \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM PresidentialCandidates");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "1. Presidential Candidates \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "1. Wagombeaji Urais \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "1. Presidential Candidates \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "1. Wagombeaji Urais \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //Get presidential candidate referenceID
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM PresidentialCandidates WHERE ID="+id);
            if(rs.next()) {
                preziRefIID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid Presidential Candidate ID.\n"
                        + "Please try again. \n"
                        + "1. Presidential Candidates \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "1. Wagombeaji Urais \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid response.\n"
                + "Try again: \n"
                + "1. Continue \n";
                this.updatedOriginPage = this.page_ERROR;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Jibu lisiloeleweka.\n"
                + "Jaribu tena: \n"
                + "2. Endelea \n";
                this.updatedOriginPage = this.page_ERROR;
            }
        }
        
        if (correctCandidateID) {
            
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON PRESIDENTIAL POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into prezi lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_PREZI_POLLING(electorPollingNo,votedPresidentRefID,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+preziRefIID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem encountered with your poll.\n"
                    + "Try again: \n"
                    + "1. Continue \n";
                    this.updatedOriginPage = this.page_ERROR;
                    return;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako wakati huu.\n"
                    + "Jaribu tena: \n"
                    + "2. Endelea \n";
                    this.updatedOriginPage = this.page_ERROR;
                    return;
                }
                this.updatedOriginPage = this.page_ERROR;
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE PresidentialCandidates SET votes=votes+1 "
                        + "WHERE referenceID='"+preziRefIID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL PRESIDENTIAL POLLING!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA RAIS IMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n"
                            + "Kamilisha kama umemaliza uchaguzi.";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem completing your polling.\n"
                    + "Try again: \n"
                    + "1. Continue \n";
                    this.updatedOriginPage = this.page_ERROR;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako.\n"
                    + "Jaribu tena: \n"
                    + "2. Endelea \n";
                    this.updatedOriginPage = this.page_ERROR;
                }
            }
            
        }
    }
    
    /**
     * Always called on prezi polling to prevent weekly double polling
     * @return 
     */
    private boolean preziWeeklyPollAlreadyCast()
    {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_PREZI_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="MP POLLING">
    
    /**
     * USSD session to poll for MP - en-lang
     */
    private void searchForAConstituencyEnglish() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        this.response = ""; //always
        this.response = "CON Enter Constituency Name: \n";
        this.updatedOriginPage = this.page_searchConstit;
    }
    
    /**
     * USSD session to poll for MP - sw-lang
     */
    private void searchForAConstituencySwahili() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        this.response = ""; //always
        this.response = "CON Weka jina la jimbo: \n";
        this.updatedOriginPage = this.page_searchConstit;
    }
    
    /**
     * Confirm searched constituency
     */
    private void confirmSearchedConstituency() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String msg = "END http://e-utafiti.com \n", 
                result = "";
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT name FROM Constituency "
                    + "WHERE name LIKE '%"+this.currentSelection+"%'");
            if (rs.next()) {
                result = rs.getString(1);// return only the first match, if more than one match
                if (this.languageSelection.equals("1")) {
                    msg = "CON Did you mean "+result+"? \n"
                            + "REPLY WITH NUMBER ONLY, i.e. 1 or 2 or 3: \n"
                            + "1. Yes \n"
                            + "2. No \n"
                            + "3. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmConstit;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Unamaanisha "+result+"? \n"
                            + "JIBU NA NAMBARI PEKEE, 1 au 2 au 3: \n"
                            + "1. Ndio \n"
                            + "2. Hapana \n"
                            + "3. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmConstit;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON Constituency "+this.currentSelection+" not found.\n"
                            + "1. Search again... \n"
                            + "2. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_constitNotFound;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hakujapatikana jimbo kama uliopendekeza ya "+this.currentSelection+": \n"
                            + "1. Tafuta tena... \n"
                            + "2. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_constitNotFound;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search Constituencies, Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * Search for MPs of provided constituency from USSD session
     */
    private void getMPsOfSelectedConstituency() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String msg = "END http://e-utafiti.com \n", 
                constRefID = "", 
                selectedConstituency = "";
        
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                    + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        
        if(mpWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one MP poll per week per person. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu, katika kila kiwango, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n";
                this.response += "3. Wawakilishi wa Wanawake \n";
                this.response += "4. Wagombeaji Seneti \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String constituency = this.previousSelection;
        
        //first get constit refID, and real name
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT constRefID, name FROM Constituency "
                    + "WHERE name LIKE '%"+constituency+"%'");
            if (rs.next()) {
                constRefID = rs.getString(1);// return only the first match, if more than one match
                selectedConstituency = rs.getString(2);
                
                if (this.languageSelection.equals("1")) {
                    msg = ""; //always
                    msg = "CON Who is you preferd MP? \n";
                    msg += "REPLY WITH NUMBER ONLY e.g 1: \n";
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = ""; //always
                    msg = "CON Chagua mgombeaji bunge unaemfaa: \n";
                    msg += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
                    this.response = msg;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Connect to Constituency... Error.... \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Timiza jimbo... shida... \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search Constituencies... Network Failure. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        String candidates = "";
        int counter = 1;
        //get mps of constituency
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT ID, JSON_EXTRACT(mP_details, '$._MP_Name') FROM MP "
                    + "WHERE constitRefID='"+constRefID+"'");
            
            while(rs.next()) {
                candidates += counter++ +/*rs.getString(1)+*/". "+rs.getString(2)+".\n";
            }
            if (!candidates.equals("")) {
                this.response += candidates;
                this.response = this.response.replace("\"", "");
                this.updatedOriginPage = this.page_pickYourPreferedMP;
            } else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON No MPs found for "+selectedConstituency+" at the moment.\n"
                            + "Continue polling...\n";
                    msg += "1. Presidential Candidates \n";
                    msg += "2. MPs \n";
                    msg += "3. Women Reps \n";
                    msg += "4. Senators \n";
                    msg += "5. Governors \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hamna wagombeaji bunge wa "+selectedConstituency+" kwa sasa.\n"
                            + "Endelea na uchaguzi...\n";
                    msg += "1. Wagombeaji Urais \n";
                    msg += "2. Wagombeaji bunge \n";
                    msg += "3. Wawakilishi wa Wanawake \n";
                    msg += "4. Wagombeaji Seneti \n";
                    msg += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Couldn't get MPs of "+selectedConstituency+" at the moment. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika ukamilishaji wa wagombeaji bunge wa "+selectedConstituency+" kwa sasa.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * Cast a vote for a selected MP 
     */
    private void mpWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(mpWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON You can only poll for MPs once per week. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji bunge ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n";
                this.response += "3. Wawakilishi wa Wanawake \n";
                this.response += "4. Wagombeaji Seneti \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String mpRefIID = "";
        boolean correctCandidateID = false;
        int id = 0;
        
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid MP ID.\n"
                        + "Please try again. \n"
                        + "2. MPs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena.\n"
                        + "2. Wagombeaji Bunge \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM MP");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "2. MPs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "2. Wagombeaji Bunge \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "2. MPs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "2. Wagombeaji Bunge \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //Get MP's reference ID 
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM MP WHERE ID="+id);
            if(rs.next()) {
                mpRefIID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid MP ID.\n"
                            + "Please try again. \n"
                            + "2. MPs \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "2. Wagombeaji Bunge \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid response. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Jibu lisiloeleweka.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
        
        if (correctCandidateID) {
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON MP POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "1. Presidential Candidates \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into mp lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_MP_POLLING(electorPollingNo,votedMPRefID,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+mpRefIID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem encountered with your MP poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la mgombeaji bunge wakati huu.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE MP SET votes=votes+1 "
                        + "WHERE referenceID='"+mpRefIID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL MP POLLING!! \n"
                        + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA MBUNGE LIMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "1. Wagombeaji Urais \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n"
                            + "Kamilisha kama umemaliza uchaguzi. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem completing your MP polling. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako mgombeaji bunge.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
            }
        }
    }
    
    /**
     * Always called on MP polling to prevent double polling per week 
     * @return 
     */
    private boolean mpWeeklyPollAlreadyCast()
    {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_MP_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="SENATOR POLLING">
    
    /**
     * 
     */
    private void searchForACountySen() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if (this.languageSelection.equals("1")) {
            this.response = ""; //always
            this.response = "CON Enter County Name: \n";
            this.updatedOriginPage = this.page_searchCountySen;
        } else if (this.languageSelection.equals("2")) {
            this.response = ""; //always
            this.response = "CON Weka jina la kata: \n";
            this.updatedOriginPage = this.page_searchCountySen;
        }
        
    }
    
    /**
     * Confirm searched county
     */
    private void confirmSearchedCounty_Sen() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "", 
                msg = "END http://e-utafiti.com \n", 
                result = "";
        
        county = this.currentSelection;
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                result = rs.getString(1);// return only the first match, if more than one match
                if (this.languageSelection.equals("1")) {
                    msg = "CON Did you mean "+result+" County? \n"
                            + "REPLY WITH NUMBER ONLY, i.e. 1 or 2 or 3: \n"
                            + "1. Yes \n"
                            + "2. No \n"
                            + "3. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountySens;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Unamaanisha kata ya "+result+"? \n"
                            + "JIBU NA NAMBARI PEKEE, 1 au 2 au 3: \n"
                            + "1. Ndio \n"
                            + "2. Hapana \n"
                            + "3. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountySens;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON County "+this.currentSelection+" not found.\n"
                            + "1. Search again... \n"
                            + "2. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundSens;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hakujapatikana kata kama uliopendekeza ya "+this.currentSelection+": \n"
                            + "1. Tafuta tena... \n"
                            + "2. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundSens;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search Counties, Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * self described
     */
    private void getSenatorsOfSelectedCounty() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "",
                msg = "END http://e-utafiti.com \n", 
                countyRefID = "", 
                selectedCounty = "";
        
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                    + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }   
        if(senatorWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one poll per week for Senators. \n"
                        + "Select another category.\n "
                        + "1. Presidential Candidates \n"
                        + "2. MPs \n "
                        + "3. Women Reps \n "
                        + "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu kwa Seneta, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n "
                        + "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji bunge \n"
                        + "3. Wawakilishi wa Wanawake \n"
                        + "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        county = this.previousSelection;
        
        //first get county refID, and real name
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT countyRefID, name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                countyRefID = rs.getString(1);// return only the first match, if more than one match
                selectedCounty = rs.getString(2);
                if (this.languageSelection.equals("1")) {
                    msg = ""; //always
                    msg = "CON Who is your preferd Senator? \n";
                    msg += "REPLY WITH NUMBER ONLY e.g. 1: \n";
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = ""; //always
                    msg = "CON Chagua Seneta unaemfaa: \n";
                    msg += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
                    this.response = msg;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Connect to County... Error... \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Timiza kata... shida... \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search County... Network Failure. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo katika kutimiza kata.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        String candidates = "";
        int counter = 1;
        //get senators of county
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT ID, JSON_EXTRACT(senator_Details, '$.senatorName') FROM Senator "
                    + "WHERE countyRefID='"+countyRefID+"'");
            while(rs.next()) {
                candidates += counter++ /*rs.getString(1)*/+". "+rs.getString(2)+".\n";
                //this.response += msg;
            }
            if (!candidates.equals("")) {
                this.response += candidates;
                this.response = this.response.replace("\"", "");
                this.updatedOriginPage = this.page_pickYourPreferedSenator;
            } else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON No Senators found for "+selectedCounty+" at the moment.\n"
                            + "Continue polling...\n";
                    msg += "1. Presidential Candidates \n";
                    msg += "2. MPs \n";
                    msg += "3. Women Reps \n";
                    msg += "4. Senators \n";
                    msg += "5. Governors \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hamna Maseneta wa "+selectedCounty+" kwa sasa.\n"
                            + "Endelea na uchaguzi...\n";
                    msg += "1. Wagombeaji Urais \n";
                    msg += "2. Wagombeaji bunge \n";
                    msg += "3. Wawakilishi wa Wanawake \n";
                    msg += "4. Wagombeaji Seneti \n";
                    msg += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Couldn't get Senators of "+selectedCounty+" at the moment. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika ukamilishaji wa wagombeaji seneti wa "+selectedCounty+" kwa sasa.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * 
     * @param sc 
     */
    private void senatorWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(senatorWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one Senatorial polling per week. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji useneta ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji Bunge\n";
                this.response += "3. Wawakilishi wa Wanawake \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String senRefID = "";
        boolean correctCandidateID = false;
        int id = 0;
        
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid Senator ID.\n"
                        + "Please try again. \n"
                        + "4. Senators \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako la seneta halitambuliki.\n"
                        + "Jaribu tena.\n"
                        + "4. Wagombeaji Seneti \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM Senator");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "4. Senators \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "4. Wagombeaji Seneti \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error collecting senators.\n"
                        + "Please try again. \n"
                        + "4. Senators \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza maseneta.\n"
                        + "Jaribu tena. \n"
                        + "4. Wagombeaji Seneti \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //Get MP's reference ID 
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM Senator WHERE ID="+id);
            if(rs.next()) {
                senRefID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid Senator ID.\n"
                            + "Please try again. \n"
                            + "4. Senators \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako la Seneta halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "4. Wagombeaji Seneti \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        if (correctCandidateID) {
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON SENATOR POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into mp lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_SENATOR_POLLING(electorPollingNo,votedSenatorRefID,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+senRefID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem encountered with your Senator poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la mgombeaji seneti.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE Senator SET votes=votes+1 "
                        + "WHERE referenceID='"+senRefID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL SENATORIAL POLLING!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA SENETA IMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n"
                            + "Kamilisha kama umemaliza uchaguzi. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem completing your Senatorial polling. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako mgombeaji seneti.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
            }
        }
    }
    
    /**
     * senator Weekly PollAlreadyCast
     * @return 
     */
    private boolean senatorWeeklyPollAlreadyCast() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_SENATOR_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="WOMENREPS POLLING">
    
    /**
     * 
     */
    private void searchForACountyWRep() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if (this.languageSelection.equals("1")) {
            this.response = ""; //always
            this.response = "CON Enter County Name: \n";
            this.updatedOriginPage = this.page_searchCountyWRep;
        } else if (this.languageSelection.equals("2")) {
            this.response = ""; //always
            this.response = "CON Weka jina la kata: \n";
            this.updatedOriginPage = this.page_searchCountyWRep;
        }
        
    }
    
    /**
     * Confirm searched county
     */
    private void confirmSearchedCounty_WR() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "", 
                msg = "END http://e-utafiti.com", 
                result = "";
        
        county = this.currentSelection;
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                result = rs.getString(1);// return only the first match, if more than one match
                if (this.languageSelection.equals("1")) {
                    msg = "CON Did you mean "+result+" County? \n"
                            + "REPLY WITH NUMBER ONLY, i.e. 1 or 2 or 3: \n"
                            + "1. Yes \n"
                            + "2. No \n"
                            + "3. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountyWreps;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Unamaanisha kata ya "+result+"? \n"
                            + "JIBU NA NAMBARI PEKEE, 1 au 2 au 3: \n"
                            + "1. Ndio \n"
                            + "2. Hapana \n"
                            + "3. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountyWreps;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON County "+county+" not found. \n"
                            + "1. Search again... \n"
                            + "2. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundWReps;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hakujapatikana kata kama uliopendekeza ya "+county+": \n"
                            + "1. Tafuta tena... \n"
                            + "2. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundWReps;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search County, Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo katika kutafuta kata... \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * 
     */
    private void getWomenRepsOfSelectedCounty() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "",
                msg = "END http://e-utafiti.com \n", 
                countyRefID = "", 
                selectedCounty = "";
        
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = "END Polling is currently closed.\n"
                        + "Please back later. \n";
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                    + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        
        if(womenRepWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one Women Reps polling per week. \n"
                        + "Select another category.\n "
                        + "1. Presidential Candidates \n"
                        + "2. MPs \n "
                        + "4. Senators \n "
                        + "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu kwa Wawakilishi wa Wanawake, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n "
                        + "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji bunge \n"
                        + "4. Wogombeaji Seneti \n"
                        + "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
        }
        
        county = this.previousSelection;
        
        //first get county refID, and real name
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT countyRefID, name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                countyRefID = rs.getString(1);// return only the first match, if more than one match
                selectedCounty = rs.getString(2);
                
                if (this.languageSelection.equals("1")) {
                    msg = ""; //always
                    msg = "CON Who is your prefered Women Rep? \n";
                    msg += "REPLY WITH NUMBER ONLY e.g. 1: \n";
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = ""; //always
                    msg = "CON Chagua Mwakilishi wa Wanawake unaemfaa: \n";
                    msg += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
                    this.response = msg;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Connect to County... Error... \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Timiza kata... shida... \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Get county Women Reps... Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida kukamilisha Wawakilishi wa Wanawake wa kata... \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        String canddates = "";
        int counter = 1;
        //get senators of county
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT ID, JSON_EXTRACT(womenRep_Details, '$.womenRepName') FROM WomenRep "
                    + "WHERE countyRefID='"+countyRefID+"'");
            
            while(rs.next()) {
                canddates += counter++ + /*rs.getString(1)+*/". "+rs.getString(2)+".\n";
            }
            if (!canddates.equals("")) {
                this.response += canddates;
                this.response = this.response.replace("\"", "");
                this.updatedOriginPage = this.page_pickYourPreferedWomenRep;
            } else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON No Women Reps. found for "+selectedCounty+" at the moment.\n"
                            + "Continue polling...\n";
                    msg += "1. Presidential Candidates \n";
                    msg += "2. MPs \n";
                    msg += "3. Women Reps \n";
                    msg += "4. Senators \n";
                    msg += "5. Governors \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hamna Wawakilishi wa Wanawake wa kata ya "+selectedCounty+" kwa sasa.\n"
                            + "Endelea na uchaguzi...\n";
                    msg += "1. Wagombeaji Urais \n";
                    msg += "2. Wagombeaji bunge \n";
                    msg += "3. Wawakilishi wa Wanawake \n";
                    msg += "4. Wagombeaji Seneti \n";
                    msg += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Couldn't get Women Reps. of "+selectedCounty+" at the moment \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika ukamilishaji wa Wawakilishi wa Wanawake wa "+selectedCounty+" \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * 
     * @param sc 
     */
    private void womenRepWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(womenRepWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one Women Reps polingl per week. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wawakilishi wa wanawake ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji Bunge\n";
                this.response += "4. Wagombeaji Seneti \n";
                this.response += "5. Wagombeaji Ugavana \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String wRepRefID = "";
        boolean correctCandidateID = false;
        int id = 0;
        
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid Women Rep ID.\n"
                        + "Please try again. \n"
                        + "3. Women Reps \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako la Mwakilishi wa Wanawake halitambuliki.\n"
                        + "Jaribu tena.\n"
                        + "3. Wawakilishi wa Wanawake \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM WomenRep");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "3. Women Reps \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "3. Wawakilishi wa Wanawake \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "3. Women Reps \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "3. Wawakilishi wa Wanawake \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //get refId
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM WomenRep WHERE ID="+id);
            if(rs.next()) {
                wRepRefID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid Women Rep ID.\n"
                            + "Please try again. \n"
                            + "3. Women Reps \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako la Mwakilishi wa Wanawake halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "3. Wawakilishi wa Wanawake \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Problem completing your Women Rep polling. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kukamilisha chaguo lako la mwakilishi wa wanawake.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
        
        if (correctCandidateID) {
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON WOMEN REP. POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "4. Senators \n";
                this.response += "5. Governors \n"
                        + "6. MCAs \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into mp lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_WOMENREP_POLLING(electorPollingNo,votedWomenRepRefId,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+wRepRefID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem encountered with your Women Rep. poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la Mwakilishi wa Wanawake wakati huu.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE WomenRep SET votes=votes+1 "
                        + "WHERE referenceID='"+wRepRefID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL WOMEN REP. POLLING!! \n"
                             + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA SENETA LIMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "4. Wagombeaji Seneti \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n"
                            + "Kamilisha kama umemaliza uchaguzi. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem completing your Women Rep polling. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la Mwakilishi wa Wanawake..\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
            }
        }
    }
    
    /**
     * Serves its so said utility
     * @return 
     */
    private boolean womenRepWeeklyPollAlreadyCast() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_WOMENREP_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="GOVERNOR POLLING">
    
    /**
     * 
     */
    private void searchForACountyGov() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if (this.languageSelection.equals("1")) {
            this.response = ""; //always
            this.response = "CON Enter County Name: \n";
            this.updatedOriginPage = this.page_searchCountyGov;
        } else if (this.languageSelection.equals("2")) {
            this.response = ""; //always
            this.response = "CON Weka jina la kata: \n";
            this.updatedOriginPage = this.page_searchCountyGov;
        }
        
    }
    
    /**
     * confirmSearchedCounty_Sen governors
     */
    private void confirmSearchedCounty_Gv() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "", 
                msg = "END http://e-utafiti.com \n", 
                result = "";
        
        county = this.currentSelection;
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                result = rs.getString(1);// return only the first match, if more than one match
                if (this.languageSelection.equals("1")) {
                    msg = "CON Did you mean "+result+" County? \n"
                            + "REPLY WITH NUMBER ONLY i.e. 1 or 2 or 3: \n"
                            + "1. Yes \n"
                            + "2. No \n"
                            + "3. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountyGovs;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Unamaanisha kata ya "+result+"? \n"
                            + "JIBU NA NAMBARI PEKEE, 1 au 2 au 3: \n"
                            + "1. Ndio \n"
                            + "2. Hapana \n"
                            + "3. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmCountyGovs;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON County "+county+" not found.\n"
                            + "1. Search again... \n"
                            + "2. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundGovs;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hakujapatikana kata kama uliopendekeza ya "+county+": \n"
                            + "1. Tafuta tena... \n"
                            + "2. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_countyNotFoundGovs;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Your suggested county "+county+" not found. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kata uliopendekeza ya "+county+" hamjapatikana \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * getGovernorsOfSelectedCounty
     */
    private void getGovernorsOfSelectedCounty() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String county = "",
                msg = "END http://e-utafiti.com \n", 
                countyRefID = "", 
                selectedCounty = "";
        
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                    + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }   
        if(gorvenorWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one Gubernatorial poll per week. \n"
                        + "Select another category.\n "
                        + "1. Presidential Candidates \n"
                        + "2. MPs \n "
                        + "3. Women Reps \n "
                        + "4. Senators \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu kwa Gavana, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n "
                        + "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji bunge \n"
                        + "3. Wawakilishi wa Wanawake \n"
                        + "4. Wagombeaji Seneti \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        county = this.previousSelection;
        
        //first get county refID, and real name
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT countyRefID, name FROM County "
                    + "WHERE name LIKE '%"+county+"%'");
            if (rs.next()) {
                countyRefID = rs.getString(1);// return only the first match, if more than one match
                selectedCounty = rs.getString(2);
                if (this.languageSelection.equals("1")) {
                    msg = ""; //always
                    msg = "CON Who is you prefered Governor? \n";
                    msg += "REPLY WITH NUMBER ONLY e.g 1: \n";
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = ""; //always
                    msg = "CON Chagua Gavana unaemfaa: \n";
                    msg += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
                    this.response = msg;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Connect to County... Error... \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Timiza kata... shida... \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON NETWORK ERROR. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        String mps = "";
        int counter = 1;
        //get senators of county
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT ID, JSON_EXTRACT(governor_Details, '$.governorName') FROM Governor "
                    + "WHERE countyRefID='"+countyRefID+"'");
            
            while(rs.next()) {
                mps += counter++ /*+rs.getString(1)*/+". "+rs.getString(2)+".\n";
                //this.response += msg;
            }
            if (!mps.equals("")) {
                this.response += mps;
                this.response = this.response.replace("\"", "");
                this.updatedOriginPage = this.page_pickYourPreferedGovernor;
            } else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON No Governors found for "+selectedCounty+" at the moment.\n"
                            + "Continue polling...\n";
                    msg += "1. Presidential Candidates \n";
                    msg += "2. MPs \n";
                    msg += "3. Women Reps \n";
                    msg += "4. Senators \n";
                    msg += "5. Governors \n"
                            + "6. MCAs \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hamna Gavana wa "+selectedCounty+" kwa sasa.\n"
                            + "Endelea na uchaguzi...\n";
                    msg += "1. Wagombeaji Urais \n";
                    msg += "2. Wagombeaji bunge \n";
                    msg += "3. Wawakilishi wa Wanawake \n";
                    msg += "4. Wagombeaji Seneti \n";
                    msg += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Couldn't get Governors for "+selectedCounty+" at the moment. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika ukamilishaji wa wagombeaji ugavana wa "+selectedCounty+" kwa sasa.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * 
     * @param sc 
     */
    private void governorWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(gorvenorWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one Gubernatorial polling per week. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n"
                        + "3. Women Reps. \n";
                this.response += "4. Senators \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji ugavana ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji Bunge\n"
                        + "3. Wawakilishi wa Wanawake";
                this.response += "4. Wagombeaji Seneti \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        String govRefID = "";
        boolean correctCandidateID = false;
        int id = 0;
        
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid Governor ID.\n"
                        + "Please try again. \n"
                        + "5. Governors \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako la Gavana halitambuliki.\n"
                        + "Jaribu tena.\n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM Governor");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your selection.\n"
                        + "Please try again. \n"
                        + "5. Governors \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako.\n"
                        + "Jaribu tena. \n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your Governor Polling.\n"
                        + "Please try again. \n"
                        + "5. Governors \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiza chaguo lako la Gavana.\n"
                        + "Jaribu tena. \n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //GET REF ID
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM Governor WHERE ID="+id);
            if(rs.next()) {
                govRefID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid Governor ID.\n"
                            + "Please try again. \n"
                            + "5. Governors \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako la Gavana halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "5. Wagombeaji Ugavana \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your Governor Polling. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiza chaguo lako la Gavana.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        if (correctCandidateID) {
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON GOVERNOR POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n"
                        + "6. MCAs \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into mp lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_GOVERNOR_POLLING(electorPollingNo,votedGovernorRefID,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+govRefID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Error processing your Governor Polling. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kutimiza chaguo lako la Gavana.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE Governor SET votes=votes+1 "
                        + "WHERE referenceID='"+govRefID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL Governor POLL!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n"
                            + "6. MCAs \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA GAVANA LIMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n"
                            + "6. MCAs \n"
                            + "Kamilisha kama umemaliza uchaguzi. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Error casting your Governor Poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la Gavana.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
            }
        }
    }
    
    /**
     * gorvenorWeeklyPollAlreadyCast
     * @return 
     */
    private boolean gorvenorWeeklyPollAlreadyCast() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_GOVERNOR_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="MCA POLLING">
    
    /**
     * 
     */
    private void searchForAWard() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if (this.languageSelection.equals("1")) {
            this.response = ""; //always
            this.response = "CON Enter Ward name: \n";
            this.updatedOriginPage = this.page_searchWard;
        } else if (this.languageSelection.equals("2")) {
            this.response = ""; //always
            this.response = "CON Weka jina la wadi: \n";
            this.updatedOriginPage = this.page_searchWard;
        }
    }
    
    /**
     * 
     */
    private void confirmSearchedWard() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String ward = "", 
                msg = "END http://e-utafiti.com \n", 
                result = "";
        
        ward = this.currentSelection;
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT name FROM WARD "
                    + "WHERE name LIKE '%"+ward+"%'");
            if (rs.next()) {
                result = rs.getString(1);// return only the first match, if more than one match
                if (this.languageSelection.equals("1")) {
                    msg = "CON Did you mean "+result+" Ward? \n"
                            + "REPLY WITH NUMBER ONLY, i.e. 1 or 2 or 3: \n"
                            + "1. Yes \n"
                            + "2. No \n"
                            + "3. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmWard;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Unamaanisha wadi ya "+result+"? \n"
                            + "JIBU NA NAMBARI PEKEE, 1 au 2 au 3: \n"
                            + "1. Ndio \n"
                            + "2. Hapana \n"
                            + "3. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_confirmWard;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    msg = "CON Ward "+ward+" not found.\n"
                            + "1. Search again... \n"
                            + "2. RETURN \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_wardNotFound;
                    return;
                }
                if (this.languageSelection.equals("2")) {
                    msg = "CON Hakujapatikana wadi kama uliopendekeza ya "+ward+": \n"
                            + "1. Tafuta tena... \n"
                            + "2. RUDI \n";
                    this.response = msg;
                    this.updatedOriginPage = this.page_wardNotFound;
                }
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Search Wards, Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo, kwa kutafuta wadi.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * self described getMCAsOfSelectedWard
     */
    private void getMCAsOfSelectedWard() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        String ward = "",
                msg = "END http://e-utafiti.com \n", 
                wardRefID = "", 
                selectedWard = "";
        
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                    + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }   
        if(mcaWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one MCA polling per week. \n"
                        + "Select another category.\n "
                        + "1. Presidential Candidates \n"
                        + "2. MPs \n "
                        + "3. Women Reps \n "
                        + "4. Senators \n"
                        + "5. Governors \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura moja tu kwa MCA, kila wiki. \n"
                        + "Chagua kiwango nyingine.\n "
                        + "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji bunge \n"
                        + "3. Wawakilishi wa Wanawake \n"
                        + "4. Wagombeaji Seneti \n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        ward = this.previousSelection;
        
        //first get ward refID, and real name
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT wardRefID, name FROM WARD "
                    + "WHERE name LIKE '%"+ward+"%'");
            if (rs.next()) {
                wardRefID = rs.getString(1);// return only the first match, if more than one match
                selectedWard = rs.getString(2);
                if (this.languageSelection.equals("1")) {
                    msg = ""; //always
                    msg = "CON Who is your prefered MCA? \n";
                    msg += "REPLY WITH NUMBER ONLY e.g 1: \n";
                    this.response = msg;
                }
                if (this.languageSelection.equals("2")) {
                    msg = ""; //always
                    msg = "CON Chagua MCA unaemfaa: \n";
                    msg += "JIBU NA NAMBARI PEKEE, kwa mfano 1: \n";
                    this.response = msg;
                }
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Connect to Ward... Error... \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Timiza Wadi... shida... \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Connect to Ward... Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Timiza Wadi... shida... \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        // prepare request for candidate entry
        if (this.languageSelection.equals("1")) {
            this.response = "";
            this.response = "CON Do you have a prefered MCA? \n"
                    + "1. Yes \n"
                    + "2. No/Undecided \n"
                    + "3. RETURN \n"
                    + "4. FINISH POLLNG \n";
            this.updatedOriginPage = this.page_IHaveAPreferedMCA;
        } else if (this.languageSelection.equals("2")) {
            this.response = "";
            this.response = "CON Uko na chaguo ya MCA? \n"
                    + "1. Ndio \n"
                    + "2. Hapana/Sijui \n"
                    + "3. RUDI \n"
                    + "4. Maliza uchaguzi \n";
            this.updatedOriginPage = this.page_IHaveAPreferedMCA;
        }
        
        
//        String candidates = "";
//        int counter = 1;
//        //get senators of county
//        try {
//            s=conn.createStatement();
//            rs=s.executeQuery("SELECT ID, JSON_EXTRACT(mca_details, '$.mcaName') FROM MCA "
//                    + "WHERE wardRefID='"+wardRefID+"'");
//            
//            while(rs.next()) {
//                candidates += counter++ + /*rs.getString(1)+*/". "+rs.getString(2)+".\n";
//                //this.response += msg;
//            }
//            if (!candidates.equals("")) {
//                this.response += candidates;
//                this.response = this.response.replace("\"", "");
//                this.updatedOriginPage = this.page_pickYourPreferedMCA;
//            } else {
//                if (this.languageSelection.equals("1")) {
//                    msg = "CON No MCAs found for "+selectedWard+" at the moment.\n"
//                            + "Continue polling...\n";
//                    msg += "1. Presidential Candidates \n";
//                    msg += "2. MPs \n";
//                    msg += "3. Women Reps \n";
//                    msg += "4. Senators \n";
//                    msg += "5. Governors \n"
//                            + "6. MCAs \n";
//                    this.response = msg;
//                    this.updatedOriginPage = this.page_selectAPollingCategory;
//                }
//                if (this.languageSelection.equals("2")) {
//                    msg = "CON Hamna Gavana wa "+selectedWard+" kwa sasa.\n"
//                            + "Endelea na uchaguzi...\n";
//                    msg += "1. Wagombeaji Urais \n";
//                    msg += "2. Wagombeaji bunge \n";
//                    msg += "3. Wawakilishi wa Wanawake \n";
//                    msg += "4. Wagombeaji Seneti \n";
//                    msg += "5. Wagombeaji Ugavana \n"
//                            + "6. MCAs \n";
//                    this.response = msg;
//                    this.updatedOriginPage = this.page_selectAPollingCategory;
//                }
//            }
//            
//        } 
//        catch (SQLException e) {
//            if (this.languageSelection.equals("1")) {
//                this.response = ""; //always
//                this.response = "CON Couldn't get MCAs for "+selectedWard+" at the moment \n"
//                    + "Try again: \n"
//                    + "1. Continue \n";
//                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
//            } else if (this.languageSelection.equals("2")) {
//                this.response = ""; //always
//                this.response = "CON Shida katika kukamilisha MCAs wa "+selectedWard+" \n"
//                        + "Jaribu tena. \n"
//                        + "2. Endelea \n";
//                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
//            }
//        }
    }
    
    /**
     * Request MCA candidate entry
     */
    private void requestPreferedMCA() {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(mcaWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one MCA polling per week. \n"
                        + "Select another category: \n"
                        + "1. Presidential Candidates \n"
                        + "2. MPs \n"
                        + "3. Women Reps \n"
                        + "4. Senators \n"
                        + "5. Governor \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji MCA ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine: \n"
                        + "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji Bunge\n"
                        + "3. Wawakilishi wa Wanawake"
                        + "4. Wagombeaji Seneti \n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        if (this.languageSelection.equals("1")) {
            this.response = "";
            this.response = "CON Enter at least two names of "
                    + "your prefered MCA candidate. \n"
                    + "(Only one candidate please.): \n";
            this.updatedOriginPage = this.page_getEnteredPreferedMCA_candidate;
        } else if (this.languageSelection.equals("2")) {
            this.response = "";
            this.response = "CON Jibu na angalau majina mawili ya "
                    + "mgombeaji MCA unaemfaa. \n"
                    + "(Mgombeaji mmoja tu, tafadhali.): \n";
            this.updatedOriginPage = this.page_getEnteredPreferedMCA_candidate;
        }
    }
    
    /**
     * mcaWeeklyPoll
     * @param sc 
     */
    private void mcaWeeklyPoll(ServletContext sc) {
        if (!this.pollerCanAccessHere) {
            return;
        }
        if(!pollingIsOpen()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "END Polling is currently closed.\n"
                        + "Please back later.";
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "END Uchanguzi wakati huu umefungwa.\n"
                        + "Tafadhali rudi baadaye.\n";
                return;
            }
            return;
        }
        if(mcaWeeklyPollAlreadyCast()) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Only one MCA polling per week. \n"
                        + "Select another category.\n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n"
                        + "3. Women Reps. \n";
                this.response += "4. Senators \n"
                        + "5. Governor \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Kura kwa wagombeaji MCA ni moja tu kila wiki. \n"
                        + "Chagua kiwango nyingine.\n";
                this.response += "1. Wagombeaji Urais \n"
                        + "2. Wagombeaji Bunge\n"
                        + "3. Wawakilishi wa Wanawake";
                this.response += "4. Wagombeaji Seneti \n"
                        + "5. Wagombeaji Ugavana \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        // save provided candidate
        // 1st: get ward and ward refID of of candidate
        String wardLike = "", wardRefID = "", selectedWard;
        ArrayList<String> splitText = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(this.text,"*");
        while (st.hasMoreTokens()) {
            splitText.add(st.nextToken());
        }
        wardLike = splitText.get(splitText.size()-4);
        
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT wardRefID, name FROM WARD "
                    + "WHERE name LIKE '%"+wardLike+"%'");
            if (rs.next()) {
                wardRefID = rs.getString(1);
                selectedWard = rs.getString(2);
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Cannot determine MCA ward. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON WAdi la MCA halitambuliki. \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
        } 
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Dtermine MCA Ward... Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Tambua wadi la MCA... shida... \n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        
        /**********************************************************************/
        
        String mcaRefID = "";
        boolean correctCandidateID = false;
        int id = 0;
       
        try {
            id = Integer.valueOf(this.currentSelection);
        }
        catch (NumberFormatException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid MCA ID.\n"
                        + "Please try again. \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako la MCA halitambuliki.\n"
                        + "Jaribu tena.\n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            return;
        }
        
        //Collect the right candidate IDs
        ArrayList<Integer> actualIDs = new ArrayList<>();
        actualIDs.add(0); // add 0 at zero cuz there's no 0 selection, and array indices start at 0 
        
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT * FROM MCA");
            while (rs.next()) {
                actualIDs.add(rs.getInt("ID"));
            }
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error processing your MCA polling.\n"
                        + "Please try again. \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza chaguo lako la MCA.\n"
                        + "Jaribu tena. \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        if (actualIDs.size() <= 1) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Error collecting MCAs.\n"
                        + "Please try again. \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiliza MCAs.\n"
                        + "Jaribu tena. \n"
                        + "6. MCAs \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return;
            }
            this.updatedOriginPage = this.page_selectAPollingCategory;
            return;
        }
        
        try {
            //match selection to right ID
            id = actualIDs.get(id);
        }
        catch (Exception e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid choice. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Chaguo lako halitambuliki.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
            return;
        }
        
        //GET REF ID
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT referenceID FROM MCA WHERE ID="+id);
            if(rs.next()) {
                mcaRefID = rs.getString(1);
                correctCandidateID = true;
            }
            else {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Invalid MCA ID.\n"
                            + "Please try again. \n"
                            + "6. MCAs \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Chaguo lako la MCA halitambuliki.\n"
                            + "Jaribu tena.\n"
                            + "6. MCAs \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Invalid response. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Jibu lisiloeleweka.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
        
        if (correctCandidateID) {
            //check is it's a system test
            if (pollerIsPrivileged()) {
                this.response = ""; //always
                this.response = "CON MCA POLL SUCCESSFUL!! \n"
                        + "Would you like to continue polling? \n";
                this.response += "1. Presidential Candidates \n"
                        + "2. MPs \n";
                this.response += "3. Women Reps \n";
                this.response += "4. Senators \n"
                        + "5. Governors \n"
                        + "Cancel if you have finished polling. \n";
                this.updatedOriginPage = this.page_selectAPollingCategory;
                return; // do not complete poll
            }
            
            //insert the pollster's no. into mp lite poll
            try {
                s=conn.createStatement();
                s.execute("INSERT INTO "
                        + "E_MCA_POLLING(electorPollingNo,votedMCARefID,pollDate) "
                        + "VALUES(SHA2('"+this.phoneNumber+"',224), '"+mcaRefID+"', SYSDATE())");
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem encountered with your MCA poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako la mMCA. \n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
                return;
            }
            
            //raise the candidate's vote count by 1
            try {
                s=conn.createStatement();
                s.execute("UPDATE MCA SET votes=votes+1 "
                        + "WHERE referenceID='"+mcaRefID+"'");
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL MCA POLL!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n"
                            + "5. Governors \n"
                            + "Cancel if you have finished polling. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA GAVANA LIMEKAMILIKA!! \n"
                            + "Endelea na uchaguzi...? \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n"
                            + "5. Wagombeaji Ugavana\n"
                            + "Kamilisha kama umemaliza uchaguzi. \n";
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                
                //give the poller/pollster airtime
                if (!pollerHasBeenAwardedWeeklyAirtime()) {
                    awardWeeklyAirtime(sc);
                }
                if (this.awardedPollerAirtime) {
                    updatePollerWeeklyAirtimeAwardInfo(sc);
                }
            } 
            catch (SQLException e) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON Problem completing your MCA poll. \n"
                        + "Try again: \n"
                        + "1. Continue \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                } else if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON Shida katika kukamilisha chaguo lako MCA.\n"
                            + "Jaribu tena. \n"
                            + "2. Endelea \n";
                    this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
                }
            }
        }
    } 
    
    /**
     * 
     * @return 
     */
    private boolean mcaWeeklyPollAlreadyCast() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM E_MCA_POLLING "
                    + "WHERE electorPollingNo=SHA2('"+this.phoneNumber+"', 224) "
                            + "AND pollDate >= (select startWeek from POLLING_WEEK) "
                            + "AND pollDate <= (select endWeek from POLLING_WEEK) ");
            return rs.next();
        } 
        catch (SQLException e) {
            return true; //a failure on this check is as if the elector has already polled
        }
    } // </editor-fold>
    
    /**
     * Self described. if polling is open, people can poll, otherwise, nope
     * @return 
     */
    private boolean pollingIsOpen()
    {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT pollingIsOpen FROM POLLING_IS_OPEN");
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }
            return false;
        } 
        catch (SQLException e) {
            return false; //a failure on this check is as if polling is closed
        }
    }
    
    private boolean canAwardAirtime() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT canAwardAirtime FROM AIRTIME_AWARD_STATUS");
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }
            return false;
        }
        catch (SQLException e) 
        {
            return false;
        }
    }
    
    /**
     * AT API implementation to buy airtime for poller
     * @param sc 
     * always after a successful poll if poller has not already been awarded airtime
     */
    private void awardWeeklyAirtime(ServletContext sc)
    {
        if (!canAwardAirtime()) {
            return;
        }
        String username = (String)sc.getInitParameter("at_username");
        String apiKey = (String)sc.getInitParameter("at_apiKey");
        JSONArray recipients = new JSONArray();
        String phoneNumber2 = this.phoneNumber;
        String recipientStringFormat = "";
        try {
            // return this.recipients.put(new JSONObject().put("phoneNumber","+254711XXXYYY").put("amount","KES XX"));
            recipientStringFormat = recipients.put(new JSONObject().put("phoneNumber",phoneNumber2)
                    .put("amount","KES 10")).toString();
        } catch (JSONException ex) {
            this.response = "CON Network failure. Try again later.";
            return;
        }
        
        //Create an instance of our awesome gateway class and pass your credentials
        AfricasTalkingGateway gateway = new AfricasTalkingGateway(username, apiKey);
        
        try {
       
            //That's it. Hit send and we will handle the rest 
            JSONArray results = gateway.sendAirtime(recipientStringFormat);
            int length = results.length();
            for(int i = 0; i < length; i++) {
             JSONObject result = results.getJSONObject(i);
             System.out.println(result.getString("status"));
             System.out.println(result.getString("amount"));
             System.out.println(result.getString("phoneNumber"));
             System.out.println(result.getString("discount"));
             System.out.println(result.getString("requestId"));

             //Error message is important when the status is not Success
             System.out.println(result.getString("errorMessage"));
            }
            
            this.response = ""; //always
            this.response = "CON SUCCESSFUL POLL!! \n "
                    + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                    + "Enjoy your KShs. 10 airtime award!!\n";
            this.updatedOriginPage = this.page_selectAPollingCategory;
            if (this.originPage.equals(this.page_pickYourPreferedPreziCandidate)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL PRESIDENTIAL POLL!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA RAIS IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
            
            if (this.originPage.equals(this.page_pickYourPreferedMP)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL MP POLL!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA MBUNGE IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "1. Wagombeaji Urais \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
            
            if (this.originPage.equals(this.page_pickYourPreferedWomenRep)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL WOMEN REP. POLL!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "4. Senators \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA MWAKILISHI WA WANAWAKE IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "4. Wagombeaji Bunge \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
            
            if (this.originPage.equals(this.page_pickYourPreferedSenator)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL SENATORIAL POLLING!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "5. Governors \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA SENETA IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "5. Wagombeaji Ugavana \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
            
            if (this.originPage.equals(this.page_pickYourPreferedGovernor)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL GOVERNOR POLLING!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA GAVANA IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n"
                            + "6. MCAs \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
            
            if (this.originPage.equals(this.page_pickYourPreferedMCA)) {
                if (this.languageSelection.equals("1")) {
                    this.response = ""; //always
                    this.response = "CON SUCCESSFUL MCA POLLING!! \n "
                            + "Thank you for polling with Kenya 2017 Elections Polling. \n "
                            + "You've been awarded KShs. 10 airtime!! \n"
                            + "Would you like to continue polling? \n";
                    this.response += "1. Presidential Candidates \n"
                            + "2. MPs \n";
                    this.response += "3. Women Reps \n";
                    this.response += "4. Senators \n"
                            + "5. Governors \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
                if (this.languageSelection.equals("2")) {
                    this.response = ""; //always
                    this.response = "CON HONGERA. CHAGUO LAKO LA MCA IMEKAMILIKA!! \n "
                            + "Asante kwa kuchukua mda wako na Kenya 2017 Elections Polling. \n "
                            + "Umezawadiwa KShs. 10 airtime!! \n"
                            + "Endelea na uchaguzi: \n";
                    this.response += "1. Wagombeaji Urais \n"
                            + "2. Wagombeaji Bunge \n";
                    this.response += "3. Wawakilishi wa Wanawake \n";
                    this.response += "4. Wagombeaji Seneti \n"
                            + "5. Wagombeaji Ugavana \n";
                    this.awardedPollerAirtime = true;
                    this.updatedOriginPage = this.page_selectAPollingCategory;
                }
            }
        }
        catch(JSONException e){
            System.out.println(e.getMessage());
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        } catch (Exception ex) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Network Error... \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * Self descriptive
     * @param sc
     * @return 
     */
    private boolean pollerHasBeenAwardedWeeklyAirtime() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT * FROM AIRTIME_AWARD "
                    + "WHERE awardedPhoneNo=sha2('"+this.phoneNumber+"',224) "
                    + "AND awardDate >= (SELECT startWeek FROM POLLING_WEEK) "
                    + "AND awardDate <= (SELECT endWeek FROM POLLING_WEEK);");
            return rs.next();
        }
        catch (SQLException e) {
            return true; //a failure on this check is as if poller has already been awarded airtime
        }
    }
    
    private void updatePollerWeeklyAirtimeAwardInfo(ServletContext sc) {
        try {
            s=conn.createStatement();
            s.execute("INSERT INTO "
                    + "AIRTIME_AWARD(awardedPhoneNo, awardReferenceId, awardDate) "
                    + "VALUES(SHA2('"+this.phoneNumber+"', 224), MD5(UUID()), SYSDATE()) ");
        }
        catch (SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Network Error. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida ya mitambo.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**************** HANDLE GATHERING OF POLLER DEMOGRAPHIC DATA ***************************************/
    
    /**
     * Check if system has poller's basic data, i.e phone number, and efId in consequence
     * if not, then store them, and get started on requesting the rest.
     * @return 
     */
    private boolean systemHasPollerBasicData() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT phoneNo FROM POLLER WHERE "
                    + "phoneNo=SHA2('"+this.phoneNumber+"',224)");
            if (rs.next()) {
                return rs.getString(1) != null;
            }
            return false; // otherwise assume poller's basic demogrphics is not in the system
        }
        catch (SQLException e)  {
            return false; // assume poller's basic demogrphics is not in the system
        }
    }
    
    /**
     * Add poller's basic details to the system
     * @return 
     */
    private void getPollerBasicData() {
        try {
            s=conn.createStatement();
            s.execute("INSERT INTO POLLER(referenceId, phoneNo) "
                    + "VALUES(MD5(UUID()), SHA2('"+this.phoneNumber+"', 224))");
            //Start requesting demographics, start with age
            requestPollerAge();
        }
        catch(SQLException e) {
            if (this.languageSelection.equals("1")) {
                this.response = ""; //always
                this.response = "CON Unable to get your details. \n"
                    + "Try again: \n"
                    + "1. Continue \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            } else if (this.languageSelection.equals("2")) {
                this.response = ""; //always
                this.response = "CON Shida katika kutimiza jibu lako.\n"
                        + "Jaribu tena. \n"
                        + "2. Endelea \n";
                this.updatedOriginPage = this.page_welcome; // restart at supposedly welcome page
            }
        }
    }
    
    /**
     * this will ensure that before a poller can poll, he/she 
     * has been requested of and supplied one's all required demographic data, 
     * i.e age, gender, education level, and formal employment status
     * @return 
     */
    private boolean systemHasPollerFullDetails() {
        try {
            s=conn.createStatement();
            rs=s.executeQuery("SELECT age, gender, educationLevel, employmentStatus "
                    + "FROM POLLER WHERE phoneNo=SHA2('"+this.phoneNumber+"', 224) ");
            if (rs.next()) {
                if (rs.getString(1) == null) {
                    requestPollerAge();
                    return false;
                }
                if (rs.getString(2) == null) {
                    requestPollerGender();
                    return false;
                }
                if (rs.getString(4) == null) {
                    requestPollerEmploymentStatus();
                    return false;
                }
                if (rs.getString(3) == null) {
                    requestPollerEducationLevel();
                    return false;
                }
                return true;
            }
            else {
                return false; // assume system does not have the full details of the poller
            }
            //
        }
        catch (SQLException e) {}
        return false; // same assumption as above
    }
    
    /**
     * 
     */
    private void requestPollerAge() {
        setCurrentLanguageSelection(); // almost always set language first
        if (this.languageSelection.equals("1")/* || this.text.equals("1")*/) {
            this.response = ""; //always
            this.response = "CON Select your age group: \n"
                    + "REPLY WITH 1, 2, 3, 4 or 5: \n"
                    + "1. [18 - 23] \n"
                    + "2. [24 - 29] \n"
                    + "3. [30 - 35] \n"
                    + "4. [36 - 41] \n"
                    + "5. [41+ (beyond 41 yrs)] \n";
            this.updatedOriginPage = this.page_selectAge;
        } else if (this.languageSelection.equals("2")/* || this.text.equals("2")*/) {
            this.response = ""; //always
            this.response = "CON Umri wako? \n"
                    + "JIBU NA 1, 2, 3, 4 ama 5:\n"
                    + "1. [18 - 23] \n"
                    + "2. [24 - 29] \n"
                    + "3. [30 - 35] \n"
                    + "4. [36 - 41] \n"
                    + "5. [41+ (kuzidi miaka 41)] \n";
            this.updatedOriginPage = this.page_selectAge;
        }
    }
    
    private void requestPollerGender() {
        setCurrentLanguageSelection(); // almost always set language first
        if (this.languageSelection.equals("1")/* || this.text.equals("1")*/) {
            this.response = ""; //always
            this.response = "CON What's your gender? \n"
                    + "REPLY WITH 1 or 2: \n"
                    + "1. Male \n"
                    + "2. Female \n";
            this.updatedOriginPage = this.page_selectGender;
        } else if (this.languageSelection.equals("2") /*|| this.text.equals("2")*/) {
            this.response = ""; //always
            this.response = "CON Jinsia yako? \n"
                    + "JIBU NA 1 ama 2 :\n"
                    + "1. Mme \n"
                    + "2. Mke \n";
            this.updatedOriginPage = this.page_selectGender;
        }
    }
    
    private void requestPollerEmploymentStatus() {
        setCurrentLanguageSelection(); // almost always set language first
        if (this.languageSelection.equals("1") /*|| this.text.equals("1")*/) {
            this.response = ""; //always
            this.response = "CON Are you formally employed? \n"
                    + "REPLY WITH 1, 2, or 3: \n"
                    + "1. Yes \n"
                    + "2. No (Unemployed) \n"
                    + "3. Self-employed \n";
            this.updatedOriginPage = this.page_selectEmployment;
        } else if (this.languageSelection.equals("2") /*|| this.text.equals("2")*/) {
            this.response = ""; //always
            this.response = "CON Umeajiriwa? \n"
                    + "JIBU NA 1, 2, ama 3: \n"
                    + "1. Ndio \n"
                    + "2. Hapana (sina ajira) \n"
                    + "3. Nimejiajiri \n";
            this.updatedOriginPage = this.page_selectEmployment;
        }
    }
    
    private void requestPollerEducationLevel() {
        setCurrentLanguageSelection(); // almost always set language first
        if (this.languageSelection.equals("1") || this.text.equals("1")) {
            this.response = ""; //always
            this.response = "CON What is your highest level of education? \n"
                    + "REPLY WITH 1, 2, 3, or 4: \n"
                    + "1. Primary school \n"
                    + "2. Secondary school \n"
                    + "3. College \n"
                    + "4. Post-University \n";
            this.updatedOriginPage = this.page_selectEducation;
        } else if (this.languageSelection.equals("2") || this.text.equals("2")) {
            this.response = ""; //always
            this.response = "CON Ni lipi, kiwango chako cha juu ya masomo? \n"
                    + "JIBU NA 1, 2, 3, ama 4: \n"
                    + "1. Shule ya msingi \n"
                    + "2. Shule ya upili \n"
                    + "3. Chuo kikuu \n"
                    + "4. Kupita chuo kikuu \n";
            this.updatedOriginPage = this.page_selectEducation;
        }
    }
    
    private boolean addedPollerAge() {
        String ageCluster = null;
        switch (this.currentSelection) {
            case "1":
                ageCluster = "18 - 23";
                break;
            case "2":
                ageCluster = "24 - 29";
                break;
            case "3":
                ageCluster = "30 - 35";
                break;
            case "4":
                ageCluster = "36 - 41";
                break;
            case "5":
                ageCluster = "41+";
                break;
            default:
                break;
        }
        try {
            s=conn.createStatement();
            s.execute("UPDATE POLLER SET age='"+ageCluster+"' "
                    + "WHERE phoneNo=SHA2('"+this.phoneNumber+"', 224) ");
            return true;
        }
        catch (SQLException e) {
            return false; // and may the message be
        }
    }
    
    private boolean addedPollerGender() {
        String gender = null;
        if (this.currentSelection.equals("1")) {
            gender = "M";
        } else if (this.currentSelection.equals("2")) {
            gender = "F";
        } 
        try {
            s=conn.createStatement();
            s.execute("UPDATE POLLER SET gender='"+gender+"' "
                    + "WHERE phoneNo=SHA2('"+this.phoneNumber+"', 224) ");
            return true;
        }
        catch (SQLException e) {
            return false; // and may the message be
        }
    }
    
    private boolean addedPollerEmploymentStatus() {
        String employment = null;
        switch (this.currentSelection) {
            case "1":
                employment = "Yes";
                break;
            case "2":
                employment = "No";
                break; 
            case "3":
                employment = "Self-employed";
                break;
            default:
                break;
        }
        try {
            s=conn.createStatement();
            s.execute("UPDATE POLLER SET employmentStatus='"+employment+"' "
                    + "WHERE phoneNo=SHA2('"+this.phoneNumber+"', 224) ");
            return true;
        }
        catch (SQLException e) {
            return false; // and may the message be
        }
    }
    
    private boolean addedPollerEducationLevel() {
        String education = null;
        switch (this.currentSelection) {
            case "1":
                education = "Primary";
                break;
            case "2":
                education = "Secondary";
                break;
            case "3":
                education = "College";
                break;
            case "4":
                education = "Post-College";
                break;
            default:
                break;
        }
        try {
            s=conn.createStatement();
            s.execute("UPDATE POLLER SET educationLevel='"+education+"' "
                    + "WHERE phoneNo=SHA2('"+this.phoneNumber+"', 224) ");
            return true;
        }
        catch (SQLException e) {
            return false; // and may the message be
        }
    }
    
    /**
     * Makes sure a privileged poller can continuously poll 
     * and as thus access the whole USSD menu, without affecting poll
     * data
     * @return 
     */
    private boolean pollerIsPrivileged() {
        boolean isPrivileged = false;
        ArrayList<String> privilegedPoller = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(this.privilegedPollersList, ",");
        while (st.hasMoreTokens()) {
            privilegedPoller.add(st.nextToken());
        }
        
        for (String privilegedNo : privilegedPoller) {
            if (this.phoneNumber.equals(privilegedNo)) {
                isPrivileged = true;
            }
        }
        
        return isPrivileged;
    }
    
    /******************************************************************************************/
    
    ///////////// The below are utility functions, and lshould probably be moved into a util package
    
    /**
     * Clean the response text from AT api sessions, so as to make it parsable
     * as I need.
     */
    private void cleanText() {
        this.text = this.text.replace("*", "");
        this.text = this.text.replace("#", "");
        this.text = this.text.replace(">>", "");
        this.text = this.text.replace("<<", "");
    }
    
    // for if i were to use the db to achieve sequentials
    
    private String getCurrentSessionPage() {
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT referencePage FROM "
                    + "POLLER_SESSION1 WHERE phoneNo = "
                    + "SHA2('"+this.phoneNumber+"', 224) AND session = '"+this.sessionId+"' ");
            if (rs.next()) {
                return rs.getString(1).replace("\"", "");
            }
            return this.page_welcome;
        }
        catch (SQLException e) {
            return this.page_welcome;
        }
    }
    
    private boolean newPoller() {
        try {
            s = conn.createStatement();
            rs = s.executeQuery("SELECT phoneNo FROM "
                    + "POLLER_SESSION1 WHERE phoneNo = "
                    + "SHA2('"+this.phoneNumber+"', 224) ");
            return !rs.next();
        }
        catch (SQLException e) {
            return true;
        }
    }
    
    private void createNewPollerSession() {
        try {
            s = conn.createStatement();
            s.execute("INSERT INTO POLLER_SESSION1(session, referencePage, phoneNo) "
                    + "VALUES( '"+this.sessionId+"', '"+this.updatedOriginPage+"' , SHA2('"+this.phoneNumber+"', 224) ");
        }
        catch (SQLException e) {
            this.response = "END SYSTEM ERROR.\n"
                            + "Please try again later.";
        }
    }
    
    private void updateReturningPollerSession() {
        try {
            s = conn.createStatement();
            s.execute("UPDATE POLLER_SESSION1 SET session = '"+this.sessionId+"', referencePage = "+this.updatedOriginPage+" "
            + "WHERE phoneNo = SHA2('"+this.phoneNumber+"', 224) ");
        }
        catch (SQLException e) {
            this.response = "END SYSTEM ERROR.\n"
                            + "Please try again later.";
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets current language selection and returns current selection
     * @return 
     */
    private String getCurrentTextSelection() {
        if (this.text.contains("*")) {
            ArrayList<String> splitText = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(this.text,"*");
            while (st.hasMoreTokens()) {
                splitText.add(st.nextToken());
            }
            this.currentSelection = splitText.get(splitText.size()-1);
            this.previousSelection = splitText.get(splitText.size()-2);
            this.languageSelection = splitText.get(0);
            return this.currentSelection;
        } else {
            this.currentSelection = this.text;
            this.previousSelection = this.text;
            this.languageSelection = this.text;
            return this.currentSelection;
        }
    }
    
    private void setCurrentLanguageSelection() {
        
        if (this.text.contains("*")) {
            ArrayList<String> splitText = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(this.text,"*");
            while (st.hasMoreTokens()) {
                splitText.add(st.nextToken());
            }
            this.currentSelection = splitText.get(splitText.size()-1);
            this.previousSelection = splitText.get(splitText.size()-2);
            this.languageSelection = splitText.get(0);
        } else {
            this.currentSelection = this.text;
            this.previousSelection = this.text;
            this.languageSelection = this.text;
        }
    }
    
    /**
     * Release resources to avoid system crush
     */
    private void releaseResources() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (s != null) {
            try {
                s.close();
            } catch (SQLException e) { /* ignored */}
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) { /* ignored */}
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
