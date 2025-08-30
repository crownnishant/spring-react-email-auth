import {createContext, useEffect, useState} from "react";
import {AppConstants} from "../util/constants.js";
import axios from "axios";
import { toast } from "react-toastify";

export const AppContext=createContext();

export const AppContextProvider=(props) => {

// ✅ Always send cookies with every Axios request for session-based authentication    
    axios.defaults.withCredentials=true;

// Base backend API URL from constants    
    const backendURL=AppConstants.BACKEND_URL;

// State to track whether user is logged in or not    
    const [isLoggedIn, setIsLoggedIn]= useState(false);

// State to store logged-in user's profile data    
    const [userData, setUserData]= useState(false);

/**
     * ✅ Fetches the logged-in user's profile data
     * Called after successful login OR when verifying if session is active
     */    
    const getUserData= async() => {
        try{
            const response=await axios.get(backendURL+"/user");
            if(response.status ==200){
                setUserData(response.data);
            }else{
                toast.error("Unable to retrieve profile.");
            }
        }catch (error){
                toast.error(error.message);
        }
    }

 /**
     * ✅ Checks whether the user session is still valid or expired
     * - Calls `/is-authenticated` API which verifies the active session using cookies
     * - If valid, sets user as logged in & fetches latest profile data
     * - If invalid, logs user out automatically
     * - function to RETAIN the state (using sessions active)
     */ 
    const getAuthState= async ()=> {
        try{
            const response=await axios.get(backendURL+ "/is-authenticated");
            if(response.status ==200 && response.data==true){
                setIsLoggedIn(true);
                await getUserData();
            }else{
                setIsLoggedIn(false);
            }
        }catch (error) {
            // ✅ If user is simply logged out, don't show an error
            console.error(error);
    }
}
 /**
     * ✅ On first page load, automatically check if user is logged in
     * - Prevents logout when user refreshes the page
     * - Ensures user session persists until cookies expire
     * - calling getAuthState function
     */

    useEffect(() => {
        getAuthState();
    }, []);

// ✅ Shared data & functions accessible to all components via context    
    const contextValue ={
        backendURL,
        isLoggedIn, setIsLoggedIn,
        userData, setUserData,
        getUserData

    }
    return (
        <AppContext.Provider value={contextValue}>
            {props.children}
        </AppContext.Provider>
    )
}