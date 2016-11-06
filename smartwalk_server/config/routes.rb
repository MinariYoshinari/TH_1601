Rails.application.routes.draw do
  # For reigistering friends
  get 'users/friend/:id', to: 'users#friend', format: false
  post 'users/friend', to: 'users#friend_add', format: false

  # For registering profile
  get 'register/:id', to: 'register#show', format: false
  post 'register', to: 'register#register', format: false

  # For messaging
  post 'message/push', format: false
  post 'message/can_push', format: false

  # For retreving infromation
  post 'users/friend_list', format: false
  post 'users/friend_url', format: false

  # For LINE callback
  post 'callback', to: 'callback#callback', format: false
end
