import Head from "next/head";
import React from "react";

import CustomLink from "../../components/common/CustomLink";
import UserAPI from "../../lib/api/user";

const ForgotPassword = () => {
  const [isLoading, setLoading] = React.useState(false);
  const [email, setEmail] = React.useState("");
  const [message, setMessage] = React.useState("");
  const [error, setError] = React.useState("");

  const handleEmailChange = React.useCallback(
    (e) => setEmail(e.target.value),
    []
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");
    setError("");

    try {
      const { data, status } = await UserAPI.requestPasswordReset(email);
      if (status === 200) {
        setMessage(data?.passwordReset?.message || "Password reset email sent.");
      } else {
        setError(data?.errors?.email?.[0] || "An error occurred.");
      }
    } catch (err) {
      setError("An error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Head>
        <title>FORGOT PASSWORD | NEXT REALWORLD</title>
        <meta name="description" content="Reset your password" />
      </Head>
      <div className="auth-page">
        <div className="container page">
          <div className="row">
            <div className="col-md-6 offset-md-3 col-xs-12">
              <h1 className="text-xs-center">Forgot Password</h1>
              <p className="text-xs-center">
                <CustomLink href="/user/login" as="/user/login">
                  Back to login
                </CustomLink>
              </p>

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
                      type="email"
                      placeholder="Email"
                      value={email}
                      onChange={handleEmailChange}
                      required
                    />
                  </fieldset>

                  <button
                    className="btn btn-lg btn-primary pull-xs-right"
                    type="submit"
                    disabled={isLoading}
                  >
                    {isLoading ? "Sending..." : "Send Reset Link"}
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

export default ForgotPassword;
