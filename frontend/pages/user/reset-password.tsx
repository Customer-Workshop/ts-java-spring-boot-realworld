import Head from "next/head";
import Router, { useRouter } from "next/router";
import React from "react";

import CustomLink from "../../components/common/CustomLink";
import UserAPI from "../../lib/api/user";

const ResetPassword = () => {
  const router = useRouter();
  const { token } = router.query;

  const [isLoading, setLoading] = React.useState(false);
  const [password, setPassword] = React.useState("");
  const [confirmPassword, setConfirmPassword] = React.useState("");
  const [message, setMessage] = React.useState("");
  const [error, setError] = React.useState("");

  const handlePasswordChange = React.useCallback(
    (e) => setPassword(e.target.value),
    []
  );

  const handleConfirmPasswordChange = React.useCallback(
    (e) => setConfirmPassword(e.target.value),
    []
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    setLoading(true);

    try {
      const { data, status } = await UserAPI.resetPassword(token as string, password);
      if (status === 200) {
        setMessage(data?.passwordReset?.message || "Password reset successful.");
        setTimeout(() => {
          Router.push("/user/login");
        }, 2000);
      } else {
        setError(data?.passwordReset?.message || "Invalid or expired reset token.");
      }
    } catch (err) {
      setError("An error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <>
        <Head>
          <title>RESET PASSWORD | NEXT REALWORLD</title>
        </Head>
        <div className="auth-page">
          <div className="container page">
            <div className="row">
              <div className="col-md-6 offset-md-3 col-xs-12">
                <h1 className="text-xs-center">Reset Password</h1>
                <div className="alert alert-danger" role="alert">
                  Invalid reset link. Please request a new password reset.
                </div>
                <p className="text-xs-center">
                  <CustomLink href="/user/forgot-password" as="/user/forgot-password">
                    Request new reset link
                  </CustomLink>
                </p>
              </div>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Head>
        <title>RESET PASSWORD | NEXT REALWORLD</title>
        <meta name="description" content="Reset your password" />
      </Head>
      <div className="auth-page">
        <div className="container page">
          <div className="row">
            <div className="col-md-6 offset-md-3 col-xs-12">
              <h1 className="text-xs-center">Reset Password</h1>

              {message && (
                <div className="alert alert-success" role="alert">
                  {message}
                </div>
              )}

              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <fieldset>
                  <fieldset className="form-group">
                    <input
                      className="form-control form-control-lg"
                      type="password"
                      placeholder="New Password"
                      value={password}
                      onChange={handlePasswordChange}
                      required
                      minLength={8}
                    />
                  </fieldset>

                  <fieldset className="form-group">
                    <input
                      className="form-control form-control-lg"
                      type="password"
                      placeholder="Confirm Password"
                      value={confirmPassword}
                      onChange={handleConfirmPasswordChange}
                      required
                      minLength={8}
                    />
                  </fieldset>

                  <button
                    className="btn btn-lg btn-primary pull-xs-right"
                    type="submit"
                    disabled={isLoading}
                  >
                    {isLoading ? "Resetting..." : "Reset Password"}
                  </button>
                </fieldset>
              </form>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ResetPassword;
