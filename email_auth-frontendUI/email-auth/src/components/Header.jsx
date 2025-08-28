import React, { useContext } from "react";
import header from "../assets/header.jpg";
import { AppContext } from "../context/AppContext";

const Header = () => {
  const { userData, isLoggedIn } = useContext(AppContext);

  return (
    <div
      className="text-center d-flex flex-column align-items-center justify-content-center py-5 px-3"
      style={{ minHeight: "80vh" }}
    >
      {/* Logo */}
      <img src={header} alt="header" width={120} className="mb-4" />

      {/* Greeting */}
      <h5 className="fw-semibold">
        {isLoggedIn && userData?.name
          ? `Hey, ${userData.name} 👋`
          : "Hey Developer 👋"}
      </h5>

      {/* Title */}
      <h1 className="fw-bold display-5 mb-3">Welcome to our product 🚀</h1>

      {/* Subtitle */}
      <p
        className="texted-muted fs-5 mb-4"
        style={{ maxWidth: "500px" }}
      >
        Let's start with a quick product tour and you can set up authentication in no time!
      </p>

      {/* Show button only if NOT logged in */}
      {!isLoggedIn && (
        <button
          className="btn btn-outline-dark rounded-pill px-4 py-2"
          style={{
            background: "linear-gradient(90deg, #007bff, #6610f2)",
            border: "none",
            transition: "all 0.3s ease",
            fontSize: "1.1rem",
          }}
          onMouseOver={(e) => {
            e.currentTarget.style.opacity = "0.85";
            e.currentTarget.style.transform = "translateY(-3px)";
          }}
          onMouseOut={(e) => {
            e.currentTarget.style.opacity = "1";
            e.currentTarget.style.transform = "translateY(0)";
          }}
          onClick={() => (window.location.href = "/login")}
        >
          Get Started
        </button>
      )}
    </div>
  );
};

export default Header;