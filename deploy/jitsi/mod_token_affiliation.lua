local LOGLEVEL = "debug"

-- token_affilation
local DISABLE_CASCADING_SET = module:get_option_boolean(
    "disable_cascading_set", false
)

local util = module:require 'util';
local is_admin = util.is_admin;
local is_healthcheck_room = util.is_healthcheck_room
local timer = require "util.timer"
local string_format = string.format;

-- webhooks for  backend
local backend_url = os.getenv("BACKEND_WEBHOOK_URL")

if not backend_url then
    module:log("error", "Webhook URL was not set! Webhooks will not work.");
else
    module:log("info", "Webhook URL are set. URL: %s", backend_url);
end


-- Lua-string to JSON-string for safe using of curl
local function json_escape(s)
    if s == nil then
        return "null"
    end
    s = s:gsub("\\", "\\\\")
    s = s:gsub("\"", "\\\"")
    s = s:gsub("\n", "\\n")
    return '"' .. s .. '"'
end

-- sending webhooks via curl
local function send_webhook_via_curl(body_string)
    if not backend_url then 
        return 
    end

    local body = body_string:gsub("'", "'\"'\"'")

    local command = string_format(
        "curl -X POST -H 'Content-Type: application/json' -d '%s' %s --max-time 5 -s -o /dev/null &",
        body,
        backend_url
    );

    module:log(LOGLEVEL, "Executing webhook via curl: %s", command);
    os.execute(command);
end

-- send events for user (join/leave)
local function send_participant_webhook(event_type, room, occupant, user_id_from_jwt)
    local user_id = user_id_from_jwt
    
    if not user_id then
        module:log(LOGLEVEL, "Can not send webhook,  user_id is missing");
        return;
    end

    local body_template = '{"event":%s, "userId":%s, "roomName":%s, "roomDomain":%s}'

    local name, domain = room.jid:match("([^@]+)@([^@]+)")

    local body_string = string_format(body_template,
        json_escape(event_type),
        json_escape(user_id),
        json_escape(name),
        json_escape(domain)
    )
    
    send_webhook_via_curl(body_string);
end

-- send events for room (ended)
local function send_room_webhook(event_type, room)
    local body_template = '{"event":%s, "userId":null, "roomName":%s, "roomDomain":%s}'

    local name, domain = room.jid:match("([^@]+)@([^@]+)")

    local body_string = string_format(body_template,
        json_escape(event_type),
        json_escape(name),
        json_escape(domain)
    )
    
    send_webhook_via_curl(body_string);
end


-- token_affiliation
module:hook("muc-room-created", function(event)
    module:log(LOGLEVEL, 'room created, prevent auto owner');

    local room = event.room;
    local _set_affiliation = room.set_affiliation;
    room.set_affiliation = function(room, actor, jid, affiliation, reason)
        if actor == "token_affiliation_plugin" then
            return _set_affiliation(room, true, jid, affiliation, reason)
        elseif affiliation == "owner" then
            return nil, "modify", "not-acceptable"
        else
            return _set_affiliation(room, actor, jid, affiliation, reason);
        end;
    end;
end);


-- token_affiliation and webhooks for backend
module:hook("muc-occupant-joined", function (event)
    local room, occupant, session = event.room, event.occupant, event.origin
    module:log(LOGLEVEL, "Event 'muc-occupant-joined' for: %s", occupant.jid)

    if is_healthcheck_room(room.jid) or is_admin(occupant.bare_jid) then
        module:log(LOGLEVEL, "Skip  'affiliation' and 'webhook' (healthcheck/admin): %s", occupant.jid)
        return
    end

    if not session.auth_token then
        module:log(LOGLEVEL, "Skip 'affiliation' and 'webhook' (token is missing)")
        return
    end

    local context_user = session.jitsi_meet_context_user
    local userIdFromJwt = context_user and context_user.id 

    -- token_affiliation
    if not session.token_affiliation_checked then
        local affiliation = "member"

        if context_user then
            if context_user["affiliation"] == "owner" then
                affiliation = "owner"
            elseif context_user["affiliation"] == "moderator" then
                affiliation = "owner"
            elseif context_user["affiliation"] == "teacher" then
                affiliation = "owner"
            elseif context_user["moderator"] == "true" then
                affiliation = "owner"
            elseif context_user["moderator"] == true then
                affiliation = "owner"
            end
        end

        local i = 0
        local function setAffiliation()
            room:set_affiliation(true, occupant.bare_jid, affiliation)
            if DISABLE_CASCADING_SET then return end
            if i > 8 then return end
            i = i + 1
            timer.add_task(0.2 * i, setAffiliation)
        end
        setAffiliation()
        session.token_affiliation_checked = true

        module:log(LOGLEVEL, "Affiliation set to: %s", affiliation)
        room:set_affiliation("token_affiliation_plugin", occupant.bare_jid, affiliation);
    else
        module:log(LOGLEVEL, "Skip 'affiliation' (already checked)")
    end

    -- webhooks for backend
    send_participant_webhook("user_joined", room, occupant, userIdFromJwt);
end)


-- webhooks for backend
module:hook("muc-occupant-left", function(event)
    local room, occupant, session = event.room, event.occupant, event.origin
    module:log(LOGLEVEL, "Event 'muc-occupant-left' for: %s", occupant.jid)

    if session then
        local context_user = session.jitsi_meet_context_user
        local userIdFromJwt = context_user and context_user.id 
        send_participant_webhook("user_left", room, occupant, userIdFromJwt);
    end
end);

module:hook("muc-room-destroyed", function(event)
    module:log(LOGLEVEL, "Event 'muc-room-destroyed' for: %s", event.room.name)
    send_room_webhook("conference_ended", event.room);
end);
