-- EXTENSION
CREATE EXTENSION IF NOT EXISTS vector;

-- ================= USERS =================
CREATE TABLE users (
  user_id SERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  gender VARCHAR(10),
  address VARCHAR(255),
  date_of_birth DATE,
  phone_number VARCHAR(13),
  point INT DEFAULT 1,
  favorite JSONB,
  search_history JSONB,
  member_id INT,
  member_start_time TIMESTAMP,
  member_end_time TIMESTAMP,
  image TEXT,
  login_time TIMESTAMP,
  logout_time TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  status VARCHAR(50)
);

CREATE INDEX idx_users_email ON users(email);

-- ================= MEMBERSHIP =================
CREATE TABLE membership (
  member_id SERIAL PRIMARY KEY,
  member_name VARCHAR(100),
  point INT,
  description TEXT,
  use_time INT,
  benefit INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

ALTER TABLE users 
ADD CONSTRAINT fk_users_membership 
FOREIGN KEY (member_id) REFERENCES membership(member_id);

-- ================= ADMINS =================
CREATE TABLE admins (
  admin_id SERIAL PRIMARY KEY,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  email VARCHAR(200) UNIQUE,
  password VARCHAR(255),
  phone_number VARCHAR(13),
  address VARCHAR(255),
  region VARCHAR(50),
  gender VARCHAR(20),
  date_of_birth DATE,
  level INT,
  role VARCHAR(100),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  status VARCHAR(50)
);

-- ================= ADMIN LOGIN LOG =================
CREATE TABLE admin_login_logs (
  id SERIAL PRIMARY KEY,
  admin_id INT REFERENCES admins(admin_id),
  ip_address VARCHAR(45),
  user_agent TEXT,
  device JSONB,
  login_time TIMESTAMP,
  logout_time TIMESTAMP,
  status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_log_admin ON admin_login_logs(admin_id);

-- ================= CATEGORY =================
CREATE TABLE categories (
  category_id SERIAL PRIMARY KEY,
  category_name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  status BOOLEAN
);

-- ================= PRODUCT =================
CREATE TABLE products (
  product_id SERIAL PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL,
  description TEXT,
  image JSONB,
  image_description JSONB,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  limited BOOLEAN,
  start_time TIMESTAMP,
  end_time TIMESTAMP
);

CREATE INDEX idx_product_name ON products(product_name);

-- ================= SIZE =================
CREATE TABLE size (
  size_id SERIAL PRIMARY KEY,
  size INT,
  status BOOLEAN,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- ================= COLOR =================
CREATE TABLE color (
  color_id SERIAL PRIMARY KEY,
  color VARCHAR(100),
  status BOOLEAN,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- ================= PRODUCT VARIANT =================
CREATE TABLE product_variant (
  pv_id SERIAL PRIMARY KEY,
  product_id INT REFERENCES products(product_id),
  price INT,
  price_discount INT,
  size_id INT REFERENCES size(size_id),
  color_id INT REFERENCES color(color_id),
  image TEXT,
  stock INT CHECK (stock >= 0),
  sold_quantity INT DEFAULT 0,
  status BOOLEAN,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE INDEX idx_variant_product ON product_variant(product_id);

-- ================= CART =================
CREATE TABLE carts (
  cart_id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(user_id),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE INDEX idx_cart_user_id ON carts(user_id);

-- ================= CART ITEMS =================
CREATE TABLE cart_items (
  id SERIAL PRIMARY KEY,
  cart_id INT REFERENCES carts(cart_id),
  pv_id INT REFERENCES product_variant(pv_id),
  quantity INT,
  total_price INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  note VARCHAR(200),
  UNIQUE(cart_id, pv_id)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

-- ================= ORDERS =================
CREATE TABLE orders (
  order_id SERIAL PRIMARY KEY,
  order_code VARCHAR(100),
  order_transaction VARCHAR(150),
  user_id INT REFERENCES users(user_id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  total_price INT,
  tax_price INT,
  transfer_price INT,
  transfer_date DATE,
  confirm_status BOOLEAN DEFAULT FALSE,
  admin_id INT REFERENCES admins(admin_id),
  confirm_time TIMESTAMP,
  status VARCHAR(50),
  refund_status BOOLEAN,
  refund_time TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- ================= ORDER DETAIL =================
CREATE TABLE order_detail (
  id SERIAL PRIMARY KEY,
  order_id INT REFERENCES orders(order_id),
  pv_id INT REFERENCES product_variant(pv_id),
  quantity INT,
  price_at_purchase INT,
  total_price INT,
  note VARCHAR(200)
);

-- ================= VOUCHERS =================
CREATE TABLE vouchers (
  voucher_id SERIAL PRIMARY KEY,
  voucher_code VARCHAR(100),
  voucher_type BOOLEAN,
  voucher_discount INT,
  min_order_value INT,
  max_reduction_value INT,
  quantity INT,
  used_quantity INT,
  description TEXT,
  contributor VARCHAR(50),
  contributor_image TEXT,
  created_at TIMESTAMP,
  voucher_start TIMESTAMP,
  voucher_end TIMESTAMP,
  updated_at TIMESTAMP,
  status BOOLEAN
);

-- mapping voucher với order
CREATE TABLE order_voucher (
  id SERIAL PRIMARY KEY,
  order_id INT REFERENCES orders(order_id),
  voucher_id INT REFERENCES vouchers(voucher_id)
);

-- ================= CHAT AI =================
CREATE TABLE chat_ai (
  chat_id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(user_id),
  session_id VARCHAR(255),
  sender VARCHAR(20) NOT NULL,
  message TEXT NOT NULL,
  intent VARCHAR(255),
  meta JSONB,
  pv_id INT REFERENCES product_variant(pv_id),
  quantity INT,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_ai_user_id ON chat_ai(user_id);
CREATE INDEX idx_chat_ai_session ON chat_ai(session_id);
CREATE INDEX idx_chat_ai_created_at ON chat_ai(created_at DESC);

-- ================= CHAT CSKH =================
CREATE TABLE chat_cskh (
  chat_id SERIAL PRIMARY KEY,
  room_id VARCHAR(255) NOT NULL,
  sender_id INT,
  sender_role VARCHAR(20),
  message TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_room_time ON chat_cskh(room_id, created_at DESC);

-- ================= VECTOR (RAG) =================
CREATE TABLE embedding (
  embedding_id SERIAL PRIMARY KEY,
  content TEXT,
  embedding VECTOR(1536),
  metadata JSONB,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_embedding_vector 
ON embedding 
USING ivfflat (embedding vector_cosine_ops);