import React from "react";
import { Routes, Route } from "react-router-dom";

// Common
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Logout from "./pages/Logout";

// Students
import EventPage from "./pages/student/EventPage";
import ViewTickets from "./pages/student/AttendeeTickets";
import AttendeeTickets from "./pages/student/AttendeeTickets";
import ApplicantTickets from "./pages/student/ApplicantTickets";
import RSVPPage from "./pages/student/RSVPPage";

// Admins
import ClubEvents from "./pages/admin/ClubEvents";
import ChooseClub from "./pages/admin/ChooseClub";
import AdminSignout from "./pages/admin/AdminSignOut";
import ModifyClubEvent from "./pages/admin/ModifyClubEvent";
import ViewAdmins from "./pages/admin/ViewAdmin";
import AddAdmin from "./pages/admin/AddAdmin";
import FundingApplications from "./pages/admin/FundingApplications";
import CreateFundingApplication from "./pages/admin/CreateFundingApplication";
import DeleteFundingApplication from "./pages/admin/DeleteFundingApplication";
import EditFundingApplication from "./pages/admin/EditFundingApplication"; // Adding EditFundingApplication

// Faculty
import ReviewAllFundingApplications from "./pages/facultyadmin/ReviewFundingApplications";

function AppRouter() {
    return (
        <Routes>
            {/* Common Routes */}
            <Route path="/" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/logout" element={<Logout />} />
            <Route path="/signout" element={<AdminSignout />} />

            {/* Student Routes */}
            <Route path="/events" element={<EventPage />} />
            <Route
                path="/events/upcoming"
                element={<EventPage display="upcoming" />}
            />
            <Route path="/events/past" element={<EventPage display="past" />} />

            <Route path="/rsvp" element={<ViewTickets />} />
            <Route path="/rsvp/get" element={<RSVPPage />} />
            <Route path="/rsvp/attendee" element={<AttendeeTickets />} />
            <Route path="/rsvp/applicant" element={<ApplicantTickets />} />

            {/* Admin Routes */}
            <Route path="/club" element={<ChooseClub />} />
            <Route path="/club/event" element={<ClubEvents />} />
            <Route
                path="/club/event/create"
                element={<ModifyClubEvent isCreate={true} />}
            />
            <Route
                path="/club/event/edit"
                element={<ModifyClubEvent isCreate={false} />}
            />

            <Route path="/club/admin" element={<ViewAdmins />} />
            <Route path="/club/admin/add" element={<AddAdmin />} />

            <Route path="/club/funding" element={<FundingApplications />} />
            <Route
                path="/club/funding/create"
                element={<CreateFundingApplication />}
            />
            <Route
                path="/club/funding/delete"
                element={<DeleteFundingApplication />}
            />
            <Route
                path="/club/funding/edit"
                element={<EditFundingApplication />}
            />

            {/* Faculty Administrator Routes */}
            <Route
                path="/faculty/view"
                element={<ReviewAllFundingApplications />}
            />
        </Routes>
    );
}

export default AppRouter;
