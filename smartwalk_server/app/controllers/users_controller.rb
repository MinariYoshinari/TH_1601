class UsersController < ApplicationController
  def friend
    friend_token = params[:id]
    user = User.find_by(friend_token: friend_token)
    @display_name = user.display_name
    @link_target = user.friend_api_url
  end

  def friend_add
    body = request.body.read
    json = JSON.parse(body)
    friend_token = json['friend_token']
    mid = json['mid']
    user1 = User.find_by(friend_token: friend_token)
    user2 = User.find_by(mid: mid)
    if user1 == user2 or user1.friends.exists?(id: user2.id) then
      return head 204
    end
    head (User.make_friend(user1, user2) ? :ok : :bad_request)
  end

  def friend_list
    body = request.body.read
    json = JSON.parse(body)
    user_mid = json['mid']
    users = User.select_friends_of(user_mid)
    result = []
    users.each do |user|
      result.push({
                    "display_name" => user.display_name,
                    "user_id" => user.user_id,
                    "picture_url" => user.picture_url
                  })
    end
    render json: result
  end

  def friend_token
    body = request.body.read
    json = JSON.parse(body)
    user_mid = json['mid']
    token = User.find_by(mid: user_mid).friend_token
    result = { "friend_token" => token }
    render json: result
  end
end
