import React, { useContext, useEffect, useState } from "react";
import { AppContext } from "../context/AppContext";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

const Login = () => {
  const [isCreateAccount, setIsCreateAccount] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const { backendURL, setIsLoggedIn, getUserData } = useContext(AppContext);
  const navigate = useNavigate();

  // Reset form fields when toggling between login and signup
  useEffect(() => {
    setName("");
    setEmail("");
    setPassword("");
  }, [isCreateAccount]);

  const onSubmitHandler = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      axios.defaults.withCredentials = true;

      if (isCreateAccount) {
        // REGISTER API
        const { data, status } = await axios.post(`${backendURL}/register`, {
          name: name.trim(),
          email: email.trim(),
          password,
        });

        if (status === 201 || status === 200) {
          toast.success(data?.message || "Account created successfully.");
          navigate("/");
        } else {
          toast.error("Email already exists");
        }
      } else {
        // LOGIN API
        const { data, status } = await axios.post(`${backendURL}/login`, {
          email: email.trim(),
          password,
        });

        if (status === 200) {
          toast.success(data?.message || "Logged in successfully.");
          setIsLoggedIn(true);
          getUserData();
          navigate("/");
        } else {
          toast.error("Invalid credentials");
        }
      }
    } catch (error) {
      const msg = error?.response?.data?.message || error?.message || "Something went wrong";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="d-flex align-items-center justify-content-center vh-100"
      style={{ background: "linear-gradient(135deg, #f0f4ff 0%, #dfe9f3 100%)" }}
    >
      <div
        className="card shadow-lg p-4"
        style={{ width: "100%", maxWidth: "400px", borderRadius: "12px" }}
      >
        {/* Title */}
        <h2 className="text-center fw-bold mb-2">
          {isCreateAccount ? "Create Account 📝" : "Welcome Back 👋"}
        </h2>
        <p className="text-center text-muted mb-4">
          {isCreateAccount
            ? "Sign up to get started with your account"
            : "Login to continue to your dashboard"}
        </p>

        {/* Create Account Button */}
        <div className="mb-4 text-center">
          {!isCreateAccount && (
            <button
              className="btn btn-outline-primary rounded-pill px-4 fw-semibold"
              style={{ fontSize: "0.95rem" }}
              onClick={() => setIsCreateAccount(true)}
            >
              + Create New Account
            </button>
          )}
        </div>

        {/* Form */}
        <form onSubmit={onSubmitHandler} noValidate>
          {isCreateAccount && (
            <div className="mb-3">
              <label htmlFor="fullName" className="form-label fw-semibold">
                Full Name
              </label>
              <input
                id="fullName"
                type="text"
                className="form-control rounded-pill"
                placeholder="Enter your full name"
                autoComplete="name"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
          )}

          {/* Email */}
          <div className="mb-3">
            <label htmlFor="email" className="form-label fw-semibold">
              Email address
            </label>
            <input
              id="email"
              type="email"
              className="form-control rounded-pill"
              placeholder="Enter your email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          {/* Password */}
          <div className="mb-3">
            <label htmlFor="password" className="form-label fw-semibold">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-control rounded-pill"
              placeholder="Enter your password"
              autoComplete={isCreateAccount ? "new-password" : "current-password"}
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {/* Forgot password */}
          {!isCreateAccount && (
            <div className="d-flex justify-content-end mb-3">
              <a
                href="/reset-password"
                className="text-decoration-none small fw-semibold"
              >
                Forgot password?
              </a>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            className="btn w-100 rounded-pill fw-semibold py-2 text-white"
            disabled={loading}
            style={{
              background: "linear-gradient(90deg, #007bff, #6610f2)",
              border: "none",
              transition: "all 0.3s ease",
              opacity: loading ? 0.85 : 1,
            }}
          >
            {loading ? (
              <span className="d-inline-flex align-items-center gap-2">
                <span
                  className="spinner-border spinner-border-sm"
                  role="status"
                  aria-hidden="true"
                />
                {isCreateAccount ? "Creating..." : "Logging in..."}
              </span>
            ) : (
              <>{isCreateAccount ? "Create Account" : "Login"}</>
            )}
          </button>
        </form>

        {/* Toggle Link */}
        <p className="text-center mt-4 mb-0">
          {isCreateAccount ? (
            <>
              Already have an account?{" "}
              <button
                type="button"
                className="btn btn-link p-0 fw-bold"
                onClick={() => setIsCreateAccount(false)}
              >
                Login
              </button>
            </>
          ) : (
            <>
              Don’t have an account?{" "}
              <button
                type="button"
                className="btn btn-link p-0 fw-bold"
                onClick={() => setIsCreateAccount(true)}
              >
                Sign up
              </button>
            </>
          )}
        </p>
      </div>
    </div>
  );
};

export default Login;