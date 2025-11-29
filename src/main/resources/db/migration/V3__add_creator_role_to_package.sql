-- Add creatorRole column to package table
ALTER TABLE package ADD COLUMN IF NOT EXISTS creator_role VARCHAR(255);

-- Update existing packages: Set creatorRole to 'Superadmin' for packages without userId
-- (Assumption: old packages without userId were created by admin)
UPDATE package SET creator_role = 'Superadmin' WHERE creator_role IS NULL;

-- Add comment to column
COMMENT ON COLUMN package.creator_role IS 'Role of the user who created this package (Customer, Superadmin, TourPackageVendor). Used for authorization filtering.';
