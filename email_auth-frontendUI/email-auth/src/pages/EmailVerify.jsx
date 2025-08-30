import React, { useContext, useEffect, useRef, useState } from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {assets} from '../assets/assets.js';
import { AppContext } from '../context/AppContext.jsx';
import { toast } from 'react-toastify';
import axios from 'axios';

const EmailVerify = () => {

  const inputRef= useRef([]); // Refs to store all 6 OTP input fields (array of refs)
  const [loading, setLoading]= useState(false); // Loader state to disable button while verifying
  const {getUserData, isLoggedIn, userData, backendURL}= useContext(AppContext);
  const navigate=useNavigate();

   /**
   * Handle value change inside each OTP input box
   * - Ensures only numeric values are allowed
   * - Automatically moves to the next input box when a digit is entered
   */
  const handleChange= (e, index) => {
    const value =e.target.value.replace(/\D/, ""); // Allow only digits
    e.target.value=value;

// Auto-focus next box if a digit is entered and we're not at the last box 
    if(value && index < 5){
      inputRef.current[index + 1].focus();
    }
  }

   /**
   * Handle keyboard events for OTP inputs
   * - If user presses Backspace on an empty box → move focus to the previous box
   */
  const handleKeyDown = (e, index) => {
    if(e.key =="Backspace" && !e.target.value && index > 0){
      inputRef.current[index -1].focus();
    }
  }

   /**
   * Handle paste event
   * - Allows user to copy paste a full 6-digit OTP at once
   * - Distributes pasted digits into input boxes automatically
   */
  const handlePaste = (e) => {
    e.preventDefault();
    const paste= e.clipboardData.getData("text").slice(0, 6).split("");

    // Assign each pasted digit into respective input boxes
    paste.forEach((digit, i) => {
      if(inputRef.current[i]){
        inputRef.current[i].value=digit;
      }
    });
    const next= paste.length <6 ? paste.length : 5;
    inputRef.current[next].focus();
  }

// Sending OTP
  const handleVerify =async() => {
    const otp=inputRef.current.map(input => input.value).join("");
    if(otp.length !==6){
      toast.error("Please enter the 6-digit OTP");
      return;
    }
    setLoading(true);
    try{
      const response=await axios.post(backendURL+"/verify-otp", {otp});
      if(response.status ==200){
        toast.success("OTP verified successfully");
        getUserData();
        navigate("/");
      }else{
        toast.error("Invalid OTP");
      }
    }catch(error){
      toast.error("Failed to verify the OTP, Please try again.");
    }finally{
      setLoading(false);
    }
  }
// implement this to redirect user to home page once email verified
// (they should not be able to access email verify page again)
  useEffect(() => {
    isLoggedIn && userData && userData.isAccountVerified && navigate("/");
  }, [isLoggedIn, userData]);

  return (
    <div className="email-verify-container d-flex align-items-center justify-content-center vh-100 position-relative"
    style={{background: "linear-gradient(90deg, #6a5af9, #8268f9)", borderRadius: "none"}}
    >
    <Link to="/" className='position-absolute top-0 start-0 p-4 d-flex align-items-center gap-2 text-decoration'>
      <img src={assets.logo} alt="logo" height={32} width={32} />
      <span className='fs-4 fw-semibold text-light'>Authify</span>
    </Link>
    
    <div className="p-5 rounded-4 shadow bg-white" style={{width: "400px"}}>
      <h4 className='text-center fw-bold mb-2'>Email Verify OTP</h4>
      <p className='text-center mb-4'>
        Enter the 6-digit code sent to your email
      </p>

      
      <div className="d-flex justify-content-between gap-2 mb-4 text-center text-white-50 mb-2">
        {[...Array(6)].map((_, i) => (
          <input
          key={i}
          type="text"
          maxLength={1}
          className='form-control text-center fs-4 otp-input'
          ref={(el) => (inputRef.current[i] =el)}
          onChange={(e) => handleChange(e, i)}
          onKeyDown={(e) => handleKeyDown(e, i)}
          onPaste={handlePaste}
           />
        ))}

      </div>
      <button className="btn btn-primary w-100 fw-semibold" disabled={loading} onClick={handleVerify}>
        {loading ? "Verifying..." : "Verify Email"}
      </button>
    </div>
    </div>
  )
}

export default EmailVerify;