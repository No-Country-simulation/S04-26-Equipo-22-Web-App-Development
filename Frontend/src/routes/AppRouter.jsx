import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Drafts } from "../views/Drafts";
import { DraftEditor } from "../views/DraftEditor";
import ChannelPreview from "../views/ChannelPreview";
import ApprovalPage from "../views/ApprovalPage";
import LoginPage from "../views/LoginPage";
import RegisterPage from "../views/RegisterPage";
import HomePage from "../views/HomePage";
import CommunitiesPage from "../views/CommunitiesPage";
import { ProtectedRoute } from "./ProtectedRoute";
import AppLayout from "../components/layout/AppLayout";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<HomePage />} />
          <Route path="/communities" element={<CommunitiesPage />} />
          <Route path="/drafts" element={<Drafts />} />
          <Route path="/editor/:draftId/:channel" element={<DraftEditor />} />
          <Route path="/preview" element={<ChannelPreview />} />
          <Route path="/approval" element={<ApprovalPage />} />
          <Route path="/approval/:id" element={<ApprovalPage />} />
        </Route>
      </Routes> 
    </BrowserRouter>
  );
}

export default AppRouter;
